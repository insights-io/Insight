package com.meemaw.auth.tfa.challenge.service;

import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.tfa.ChallengeSessionExpiredException;
import com.meemaw.auth.tfa.MfaChallengeValidatationException;
import com.meemaw.auth.tfa.MfaMethod;
import com.meemaw.auth.tfa.MfaProvider;
import com.meemaw.auth.tfa.MfaProvidersRegistry;
import com.meemaw.auth.tfa.challenge.datasource.MfaChallengeDatasource;
import com.meemaw.auth.tfa.model.MfaConfiguration;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class MfaChallengeService {

  public static final Map<String, String> INVALID_CODE_ERRORS = Map.of("code", "Invalid code");

  @Inject MfaChallengeDatasource challengeDatasource;
  @Inject UserMfaDatasource userMfaDatasource;
  @Inject MfaProvidersRegistry mfaProvidersRegistry;
  @Inject SsoService ssoService;
  @Inject UserDatasource userDatasource;

  @Traced
  public CompletionStage<String> start(UUID userId) {
    return challengeDatasource.create(userId);
  }

  @Traced
  public CompletionStage<String> complete(MfaMethod method, int code, String challengeId) {
    return challengeDatasource
        .retrieve(challengeId)
        .thenCompose(
            maybeUserId -> {
              UUID userId =
                  maybeUserId.orElseThrow(
                      () -> {
                        log.info("[AUTH]: Challenge session {} expired", challengeId);
                        throw new ChallengeSessionExpiredException();
                      });

              return userMfaDatasource
                  .get(userId, method)
                  .thenCompose(
                      maybeTfaSetup -> {
                        MfaConfiguration mfaConfiguration =
                            maybeTfaSetup.orElseThrow(
                                () -> {
                                  log.info(
                                      "[AUTH]: {} TFA complete attempt for user={} which is not configured",
                                      method,
                                      userId);
                                  throw new ChallengeSessionExpiredException(
                                      String.format("%s TFA not configured", method));
                                });

                        MfaProvider<?> mfaProvider = mfaProvidersRegistry.get(method);
                        try {
                          return mfaProvider
                              .completeChallenge(challengeId, code, mfaConfiguration)
                              .thenCompose(
                                  isValid -> {
                                    if (!isValid) {
                                      log.info(
                                          "[AUTH]: Invalid code for {} TFA challenge for user={}",
                                          method,
                                          userId);
                                      throw Boom.badRequest()
                                          .errors(INVALID_CODE_ERRORS)
                                          .exception();
                                    }

                                    challengeDatasource.delete(challengeId);
                                    return ssoService.createSession(userId);
                                  });
                        } catch (MfaChallengeValidatationException ex) {
                          log.error(
                              "[AUTH]: Something went wrong while validating TFA code for user={}",
                              userId,
                              ex);
                          throw Boom.serverError().exception(ex);
                        }
                      });
            });
  }

  @Traced
  public CompletionStage<Optional<AuthUser>> retrieveUser(String id) {
    return challengeDatasource
        .retrieve(id)
        .thenCompose(
            maybeUserId -> {
              if (maybeUserId.isEmpty()) {
                return CompletableFuture.completedStage(Optional.empty());
              }
              // TODO: should probably use user cache
              return userDatasource.findUser(maybeUserId.get());
            });
  }
}
