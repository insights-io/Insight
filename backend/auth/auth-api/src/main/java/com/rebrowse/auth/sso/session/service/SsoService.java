package com.rebrowse.auth.sso.session.service;

import com.rebrowse.auth.accounts.model.AuthorizationSuccessResponseDTO;
import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationMfaChallengeSuccessResponse;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.accounts.model.response.DirectAuthorizationRedirectResponse;
import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.setup.service.MfaAuthorizationChallengeService;
import com.rebrowse.auth.organization.datasource.OrganizationDatasource;
import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.auth.signup.service.SignUpService;
import com.rebrowse.auth.sso.IdentityProviderRegistry;
import com.rebrowse.auth.sso.session.datasource.SsoSessionDatasource;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.sso.session.model.SsoUser;
import com.rebrowse.auth.sso.setup.datasource.SsoSetupDatasource;
import com.rebrowse.auth.sso.setup.model.SsoMethod;
import com.rebrowse.auth.sso.setup.model.dto.SsoSetupDTO;
import com.rebrowse.auth.user.datasource.UserDatasource;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.UserWithLoginInformation;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.ArrayUtils;

@ApplicationScoped
public class SsoService {

  @Inject SsoSessionDatasource ssoSessionDatasource;
  @Inject OrganizationDatasource organizationDatasource;
  @Inject MfaAuthorizationChallengeService mfaAuthorizationChallengeService;
  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject UserDatasource userDatasource;
  @Inject IdentityProviderRegistry identityProviderRegistry;
  @Inject SignUpService signUpService;

  private CompletionStage<AuthorizationResponse> authorizeSuccess(
      AuthUser user, Function<String, AuthorizationResponse> responseFunction) {
    return ssoSessionDatasource.create(user).thenApply(responseFunction);
  }

  public CompletionStage<AuthorizationResponse> authorizeMfaChallengeSuccess(
      AuthUser user, AuthorizationRequest authorizationRequest) {
    return authorizeSuccess(
        user,
        (sessionId) ->
            new AuthorizationMfaChallengeSuccessResponse(
                authorizationRequest.getRedirect(), authorizationRequest.getDomain(), sessionId));
  }

  private CompletionStage<AuthorizationResponse> authorizationRedirectResponse(
      AuthUser user, AuthorizationRequest request) {
    return authorizeSuccess(
        user,
        (sessionId) ->
            (extraCookies) ->
                Response.status(302)
                    .header(HttpHeaders.LOCATION, request.getRedirect())
                    .cookie(
                        ArrayUtils.add(
                            extraCookies, SsoSession.cookie(sessionId, request.getDomain())))
                    .build());
  }

  private CompletionStage<AuthorizationResponse> authorizationDataResponse(
      AuthUser user, AuthorizationRequest request) {
    return authorizeSuccess(
        user,
        (sessionId) ->
            (extraCookies) ->
                DataResponse.okBuilder(new AuthorizationSuccessResponseDTO(request.getRedirect()))
                    .cookie(
                        ArrayUtils.add(
                            extraCookies, SsoSession.cookie(sessionId, request.getDomain())))
                    .build());
  }

  public CompletionStage<AuthorizationResponse> authorizeSignUpSuccess(
      AuthUser user, AuthorizationRequest authorizationRequest) {
    return authorizeSuccess(
        user,
        sessionId ->
            new DirectAuthorizationRedirectResponse(
                authorizationRequest.getRedirect(), authorizationRequest.getDomain(), sessionId));
  }

  public CompletionStage<AuthorizationResponse> authorizeOrMfaChallengeNewUser(
      AuthUser user, AuthorizationRequest request) {
    return authorizeOrMfaChallenge(user, Collections.emptyList(), request);
  }

  public CompletionStage<AuthorizationResponse> authorizeOrMfaChallenge(
      UserWithLoginInformation user, AuthorizationRequest request) {
    return authorizeOrMfaChallenge(user.user(), user.getMfaMethods(), request);
  }

  public CompletionStage<AuthorizationResponse> authorizeOrMfaChallenge(
      AuthUser user, List<MfaMethod> mfaMethods, AuthorizationRequest request) {
    if (mfaMethods.isEmpty()) {
      return organizationDatasource
          .retrieve(user.getOrganizationId())
          .thenCompose(
              maybeOrganization -> {
                Organization organization =
                    maybeOrganization.orElseThrow(() -> Boom.notFound().exception());

                if (organization.isEnforceMultiFactorAuthentication()) {
                  return mfaAuthorizationChallengeService.createChallenge(
                      user.getId(), mfaMethods, request);
                }

                return this.authorizationDataResponse(user, request);
              });
    }

    return mfaAuthorizationChallengeService.createChallenge(user.getId(), mfaMethods, request);
  }

  public CompletionStage<Optional<SsoUser>> logout(String sessionId) {
    return ssoSessionDatasource.delete(sessionId);
  }

  public CompletionStage<Set<String>> logoutUserFromAllDevices(UUID userId) {
    return ssoSessionDatasource.deleteAllForUser(userId);
  }

  public CompletionStage<Set<String>> logoutFromAllDevices(String sessionId) {
    return retrieveSession(sessionId)
        .thenCompose(
            maybeUser ->
                maybeUser.isEmpty()
                    ? CompletableFuture.completedStage(Collections.emptySet())
                    : logoutUserFromAllDevices(maybeUser.get().getId()));
  }

  public CompletionStage<Set<String>> listSessions(String sessionId) {
    return ssoSessionDatasource
        .retrieve(sessionId)
        .thenCompose(
            maybeUser ->
                ssoSessionDatasource.listAllForUser(
                    maybeUser.orElseThrow(() -> Boom.notFound().exception()).getId()));
  }

  public CompletionStage<Optional<AuthUser>> retrieveSession(String sessionId) {
    return ssoSessionDatasource
        .retrieve(sessionId)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }

  public CompletionStage<AuthorizationResponse> authorizeSamlSso(
      String email, String fullName, String organizationId, URI redirect, URI serverBaseUri) {
    AuthorizationRequest request = new AuthorizationRequest(email, redirect, serverBaseUri);
    return userDatasource
        .retrieveUserWithLoginInformation(email)
        .thenCompose(
            maybeUser -> {
              if (maybeUser.isEmpty()) {
                return signUpService
                    .ssoSignUpNewMember(email, fullName, organizationId)
                    .thenCompose(user -> authorizationRedirectResponse(user, request));
              }

              AuthUser user = maybeUser.get().user();
              return authorizationRedirectResponse(user, request);
            });
  }

  public CompletionStage<AuthorizationResponse> authorizeSocialSso(
      String email, String fullName, SsoMethod method, URI redirect, URI serverBaseUri) {
    AuthorizationRequest request = new AuthorizationRequest(email, redirect, serverBaseUri);
    return userDatasource
        .retrieveUserWithLoginInformation(email)
        .thenCompose(
            maybeUser ->
                maybeUser.isEmpty()
                    ? authorizeSocialSsoNewUser(request, method, fullName)
                    : authorizeSocialSsoExistingUser(request, method, maybeUser.get()));
  }

  private CompletionStage<AuthorizationResponse> authorizeSocialSsoExistingUser(
      AuthorizationRequest request, SsoMethod method, UserWithLoginInformation user) {
    return ssoSetupDatasource
        .get(user.getOrganizationId())
        .thenCompose(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                return authorizationRedirectResponse(user.user(), request);
              }

              SsoSetupDTO ssoSetup = maybeSsoSetup.get();
              SsoMethod expectedMethod = ssoSetup.getMethod();
              if (expectedMethod.equals(method)) {
                return authorizationRedirectResponse(user.user(), request);
              }

              return identityProviderRegistry
                  .getService(expectedMethod)
                  .getSsoAuthorizationRedirectResponse(ssoSetup, request);
            });
  }

  private CompletionStage<AuthorizationResponse> authorizeSocialSsoNewUser(
      AuthorizationRequest request, SsoMethod method, String fullName) {
    String email = request.getEmail();
    String emailDomain = EmailUtils.getDomain(request.getEmail());

    if (!EmailUtils.isBusinessDomain(emailDomain)) {
      return signUpService
          .ssoSignUp(email, fullName)
          .thenCompose(user -> authorizationRedirectResponse(user, request));
    }

    return ssoSetupDatasource
        .getByDomain(emailDomain)
        .thenCompose(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                return signUpService
                    .ssoSignUp(email, fullName)
                    .thenCompose(user -> authorizationRedirectResponse(user, request));
              }

              SsoSetupDTO ssoSetup = maybeSsoSetup.get();
              String organizationId = ssoSetup.getOrganizationId();
              SsoMethod expectedMethod = ssoSetup.getMethod();

              return organizationDatasource
                  .retrieve(organizationId)
                  .thenCompose(
                      maybeOrganization -> {
                        Organization organization =
                            maybeOrganization.orElseThrow(() -> Boom.notFound().exception());

                        if (!organization.isOpenMembership()) {
                          return signUpService
                              .ssoSignUp(email, fullName)
                              .thenCompose(user -> authorizationRedirectResponse(user, request));
                        }

                        if (expectedMethod.equals(method)) {
                          return signUpService
                              .ssoSignUpNewMember(email, fullName, organizationId)
                              .thenCompose(user -> authorizationRedirectResponse(user, request));
                        }

                        return identityProviderRegistry
                            .getService(expectedMethod)
                            .getSsoAuthorizationRedirectResponse(ssoSetup, request);
                      });
            });
  }
}
