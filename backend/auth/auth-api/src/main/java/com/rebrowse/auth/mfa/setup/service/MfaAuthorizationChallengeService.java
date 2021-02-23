package com.rebrowse.auth.mfa.setup.service;

import com.rebrowse.auth.accounts.datasource.challenge.AuthorizationChallengeDatasource;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallenge;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallengeType;
import com.rebrowse.auth.accounts.model.challenge.MfaChallengeResponseDTO;
import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationMfaChallengeResponse;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.mfa.ChallengeSessionExpiredException;
import com.rebrowse.auth.mfa.MfaChallengeValidatationException;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.MfaProvidersRegistry;
import com.rebrowse.auth.mfa.dto.MfaChallengeCodeDetailsDTO;
import com.rebrowse.auth.mfa.model.MfaConfiguration;
import com.rebrowse.auth.mfa.sms.impl.MfaSmsProvider;
import com.rebrowse.auth.sso.session.service.SsoService;
import com.rebrowse.auth.user.datasource.UserDatasource;
import com.rebrowse.auth.user.datasource.UserMfaDatasource;
import com.rebrowse.auth.user.datasource.UserTable;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.rest.response.Boom;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MfaAuthorizationChallengeService {

  public static final Map<String, String> INVALID_CODE_ERRORS = Map.of("code", "Invalid code");

  @Inject AuthorizationChallengeDatasource authorizationChallengeDatasource;
  @Inject UserDatasource userDatasource;
  @Inject MfaSetupService mfaSetupService;
  @Inject UserMfaDatasource userMfaDatasource;
  @Inject SsoService ssoService;
  @Inject MfaProvidersRegistry mfaProvidersRegistry;
  @Inject MfaSmsProvider smsProvider;

  public CompletionStage<AuthorizationResponse> createChallenge(
      UUID userId, List<MfaMethod> methods, AuthorizationRequest authorizationRequest) {
    String domain = authorizationRequest.getDomain();
    URI redirect = authorizationRequest.getRedirect();
    return authorizationChallengeDatasource
        .create(AuthorizationChallenge.mfa(userId.toString(), redirect))
        .thenApply(
            challengeId -> new AuthorizationMfaChallengeResponse(challengeId, domain, methods));
  }

  public CompletionStage<Optional<MfaChallengeResponseDTO>> retrieveChallenge(String challengeId) {
    return authorizationChallengeDatasource
        .retrieve(challengeId, AuthorizationChallengeType.MFA)
        .thenCompose(
            maybeChallenge -> {
              if (maybeChallenge.isEmpty()) {
                return CompletableFuture.completedStage(Optional.empty());
              }
              UUID userId = UUID.fromString(maybeChallenge.get().getValue());
              return userMfaDatasource
                  .listMethods(userId)
                  .thenApply(methods -> Optional.of(new MfaChallengeResponseDTO(methods)));
            });
  }

  public CompletionStage<Optional<AuthUser>> retrieveChallengedUser(String challengeId) {
    return authorizationChallengeDatasource
        .retrieve(challengeId, AuthorizationChallengeType.MFA)
        .thenCompose(
            maybeMfaChallenge -> {
              if (maybeMfaChallenge.isEmpty()) {
                return CompletableFuture.completedStage(Optional.empty());
              }
              return userDatasource.retrieve(UUID.fromString(maybeMfaChallenge.get().getValue()));
            });
  }

  public CompletionStage<AuthorizationResponse> completeChallenge(
      AuthUser user, String challengeId, MfaMethod method, int code, URI serverBaseUri) {
    return authorizationChallengeDatasource
        .retrieve(challengeId, AuthorizationChallengeType.MFA)
        .thenCompose(
            maybeMfaChallenge -> {
              AuthorizationChallenge mfaChallenge =
                  maybeMfaChallenge.orElseThrow(() -> Boom.unauthorized().exception());
              UUID userId = UUID.fromString(mfaChallenge.getValue());

              return userMfaDatasource
                  .retrieve(userId, method)
                  .thenCompose(
                      maybeMfaConfiguration -> {
                        MfaConfiguration mfaConfiguration =
                            maybeMfaConfiguration.orElseThrow(
                                () ->
                                    new ChallengeSessionExpiredException(
                                        String.format("%s MFA not configured", method)));

                        AuthorizationRequest authorizationRequest =
                            new AuthorizationRequest(
                                user.getEmail(), mfaChallenge.getRedirect(), serverBaseUri);

                        try {
                          return mfaProvidersRegistry
                              .get(method)
                              .completeChallenge(challengeId, code, mfaConfiguration)
                              .thenCompose(
                                  isValid -> {
                                    if (!isValid) {
                                      throw Boom.badRequest()
                                          .errors(INVALID_CODE_ERRORS)
                                          .exception();
                                    }

                                    authorizationChallengeDatasource.delete(challengeId);
                                    return ssoService.authorizeMfaChallengeSuccess(
                                        user, authorizationRequest);
                                  });
                        } catch (MfaChallengeValidatationException ex) {
                          throw Boom.serverError().exception(ex);
                        }
                      });
            });
  }

  public CompletionStage<AuthorizationResponse> completeEnforcedChallenge(
      AuthUser user, String challengeId, MfaMethod method, int code, URI serverBaseUri) {
    return authorizationChallengeDatasource
        .retrieve(challengeId, AuthorizationChallengeType.MFA)
        .thenCompose(
            maybeMfaChallenge -> {
              AuthorizationChallenge mfaChallenge =
                  maybeMfaChallenge.orElseThrow(() -> Boom.unauthorized().exception());

              AuthorizationRequest authorizationRequest =
                  new AuthorizationRequest(
                      user.getEmail(), mfaChallenge.getRedirect(), serverBaseUri);

              return mfaSetupService
                  .completeSetup(challengeId, method, user, code)
                  .thenCompose(
                      ignored -> {
                        authorizationChallengeDatasource.delete(challengeId);
                        return ssoService.authorizeMfaChallengeSuccess(user, authorizationRequest);
                      });
            });
  }

  public CompletionStage<MfaChallengeCodeDetailsDTO> sendSmsCode(
      AuthUser user, String challengeId) {
    if (!user.isPhoneNumberVerified()) {
      throw Boom.badRequest()
          .errors(UserTable.Errors.PHONE_NUMBER_VERIFICATION_REQUIRED)
          .exception();
    }

    String codeKey = MfaSmsProvider.challengeCodeKey(challengeId);
    return smsProvider.sendVerificationCode(codeKey, user.getPhoneNumber());
  }
}
