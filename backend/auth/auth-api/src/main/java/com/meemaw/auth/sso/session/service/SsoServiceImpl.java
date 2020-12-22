package com.meemaw.auth.sso.session.service;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.mfa.ChallengeLoginResult;
import com.meemaw.auth.mfa.MfaMethod;
import com.meemaw.auth.mfa.challenge.service.MfaChallengeService;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.signup.service.SignUpService;
import com.meemaw.auth.sso.IdentityProviderRegistry;
import com.meemaw.auth.sso.session.datasource.SsoSessionDatasource;
import com.meemaw.auth.sso.session.model.DirectLoginResult;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.LoginResult;
import com.meemaw.auth.sso.session.model.RedirectSessionLoginResult;
import com.meemaw.auth.sso.session.model.ResponseLoginResult;
import com.meemaw.auth.sso.session.model.SsoUser;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.SsoSetup;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class SsoServiceImpl implements SsoService {

  @Inject OrganizationDatasource organizationDatasource;
  @Inject SsoSessionDatasource ssoSessionDatasource;
  @Inject PasswordService passwordService;
  @Inject UserDatasource userDatasource;
  @Inject SignUpService signUpService;
  @Inject MfaChallengeService mfaChallengeService;
  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject IdentityProviderRegistry identityProviderRegistry;

  @Traced
  @Timed(
      name = "createSessionForUser",
      description = "A measure of how long it takes to create session for user")
  private CompletionStage<String> createSession(AuthUser user) {
    MDC.put(LoggingConstants.USER_ID, user.getId().toString());
    MDC.put(LoggingConstants.USER_EMAIL, user.getEmail());
    MDC.put(LoggingConstants.ORGANIZATION_ID, user.getOrganizationId());
    return ssoSessionDatasource
        .create(user)
        .thenApply(
            sessionId -> {
              log.info(
                  "[AUTH]: Created session for user={} email={} SessionId={}",
                  user.getId(),
                  user.getEmail(),
                  sessionId);
              MDC.put(LoggingConstants.SSO_SESSION_ID, sessionId);
              return sessionId;
            });
  }

  @Override
  @Traced
  @Timed(
      name = "createSessionForUserId",
      description = "A measure of how long it takes to create session for user id")
  public CompletionStage<String> createSession(UUID userId) {
    return userDatasource
        .findUser(userId)
        .thenCompose(
            maybeUser ->
                createSession(
                    maybeUser.orElseThrow(
                        () -> Boom.notFound().message("User not found").exception())));
  }

  @Override
  @Traced
  @Timed(name = "findSession", description = "A measure of how long it takes to do logout")
  public CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
    MDC.put(LoggingConstants.SSO_SESSION_ID, sessionId);
    log.info("[AUTH]: Find session: {}", sessionId);
    return ssoSessionDatasource
        .retrieve(sessionId)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }

  @Override
  @Traced
  @Timed(name = "findSessions", description = "A measure of how long it takes to find SSO sessions")
  public CompletionStage<Set<String>> findSessions(String sessionId) {
    MDC.put(LoggingConstants.SSO_SESSION_ID, sessionId);
    log.info("[AUTH]: Find all sessions related to session: {}", sessionId);
    return ssoSessionDatasource
        .retrieve(sessionId)
        .thenCompose(
            maybeUser ->
                ssoSessionDatasource.listAllForUser(
                    maybeUser.orElseThrow(() -> Boom.notFound().exception()).getId()));
  }

  @Override
  @Traced
  @Timed(name = "logout", description = "A measure of how long it takes to do logout")
  public CompletionStage<Optional<SsoUser>> logout(String sessionId) {
    MDC.put(LoggingConstants.SSO_SESSION_ID, sessionId);
    log.info("[AUTH]: Logout attempt for session: {}", sessionId);
    return ssoSessionDatasource.delete(sessionId);
  }

  @Override
  @Traced
  @Timed(
      name = "logoutFromAllDevices",
      description = "A measure of how long it takes to do a logout from all devices")
  public CompletionStage<Set<String>> logoutUserFromAllDevices(UUID userId) {
    MDC.put(LoggingConstants.USER_ID, userId.toString());
    log.info("[AUTH]: Logout from all devices userId: {}", userId);
    return ssoSessionDatasource
        .deleteAllForUser(userId)
        .thenApply(
            deletedSessions -> {
              if (!deletedSessions.isEmpty()) {
                log.info(
                    "[AUTH]: Successfully logged out of {} devices  userId: {}",
                    deletedSessions.size(),
                    userId);
              }
              return deletedSessions;
            });
  }

  @Override
  @Traced
  @Timed(name = "login", description = "A measure of how long it takes to do a password login")
  public CompletionStage<LoginResult<?>> passwordLogin(
      String email, String password, String ipAddress, URI redirect, URI serverBaseUri) {
    MDC.put(LoggingConstants.USER_EMAIL, email);

    Function<Optional<SsoSetup>, CompletionStage<LoginResult<?>>> passwordLoginSupplier =
        (maybeSsoSetup) -> {
          log.info("[AUTH]: Email login attempt with password email={} ip={}", email, ipAddress);
          return passwordService
              .verifyPassword(email, password)
              .thenCompose(
                  userWithLoginInformation ->
                      authenticate(
                          userWithLoginInformation.user(),
                          userWithLoginInformation.getMfaMethods()));
        };

    Function<SsoSetup, CompletionStage<LoginResult<?>>> alternativeLoginProvider =
        ssoSetup ->
            passwordLoginSsoAlternative(
                identityProviderRegistry.ssoSignInLocation(
                    ssoSetup.getMethod(), email, serverBaseUri, redirect));

    return login(email, LoginMethod.PASSWORD, alternativeLoginProvider, passwordLoginSupplier);
  }

  /**
   * Handle case when user tries to login with email + password, but his domain has SSO setup
   * configured. Because password login is initiated with client POST request, we cannot redirect
   * directly to SSO provider because of CORS. However, we can throw a 400 request and include a
   * location which client should follow to complete flow using the configured SSO provider.
   *
   * @param ssoSignInLocation location client should go to in order to continue SSO flow
   * @return will always throw
   */
  private CompletionStage<LoginResult<?>> passwordLoginSsoAlternative(URI ssoSignInLocation) {
    throw Boom.badRequest()
        .message("SSO login required")
        .errors(Map.of("goto", ssoSignInLocation))
        .exception();
  }

  private CompletionStage<LoginResult<?>> login(
      String email,
      LoginMethod loginMethod,
      Function<SsoSetup, CompletionStage<LoginResult<?>>> alternativeLoginProvider,
      Function<Optional<SsoSetup>, CompletionStage<LoginResult<?>>> defaultLoginProvider) {
    if (!EmailUtils.isBusinessDomain(email)) {
      return defaultLoginProvider.apply(Optional.empty());
    }

    log.info("[AUTH]: Login attempt with business email={}", email);
    return ssoSetupDatasource
        .getByDomain(EmailUtils.domainFromEmail(email))
        .thenCompose(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                log.info(
                    "[AUTH]: SSO setup not configured using default login provider email={} loginMethod={} ",
                    email,
                    loginMethod);
                return defaultLoginProvider.apply(Optional.empty());
              }

              SsoSetup ssoSetup = maybeSsoSetup.get();
              SsoMethod method = ssoSetup.getMethod();
              if (method.getKey().equals(loginMethod.getKey())) {
                log.info(
                    "[AUTH]: SSO default login method matches using default login provider method={} email={}",
                    method,
                    email);
                return defaultLoginProvider.apply(Optional.of(ssoSetup));
              }

              log.info(
                  "[AUTH] Enforcing alternative SSO login provider email={} loginMethod={} method={}",
                  email,
                  loginMethod,
                  method);

              return alternativeLoginProvider.apply(ssoSetup);
            });
  }

  /**
   * Authenticate user directly by skipping MFA checks. This is the last step in the login flow.
   * This is used on the end of MFA challenge setup flow where user just completed the MFA setup so
   * he should not be challenged again.
   *
   * @param user to be authenticated
   * @param redirect to take user to
   * @return login result
   */
  @Override
  public CompletionStage<LoginResult<?>> authenticateDirect(AuthUser user, @Nullable URI redirect) {
    UUID userId = user.getId();
    return createSession(user)
        .thenApply(
            sessionId -> {
              if (redirect != null) {
                log.debug("[AUTH]: Successful login for user={} redirect={}", userId, redirect);
                return new RedirectSessionLoginResult(sessionId, redirect);
              }

              log.debug("[AUTH]: Successful login for user={}", userId);
              return new DirectLoginResult(sessionId);
            });
  }

  /**
   * Authenticate a user with no MFA methods set. This method can be used on the end of user
   * creation flows (e.g. team invite accept).
   *
   * <p>If organization has MFA enforced, user will not be directly authenticated but will have to
   * configure MFA method using challenge session.
   *
   * @param user to be authenticated
   * @param redirect to take user to
   * @return login result
   */
  @Override
  public CompletionStage<LoginResult<?>> authenticate(AuthUser user, @Nullable URI redirect) {
    return authenticate(user, Collections.emptyList(), redirect);
  }

  private CompletionStage<LoginResult<?>> authenticate(
      AuthUser user, List<MfaMethod> userMfaMethods) {
    return authenticate(user, userMfaMethods, null);
  }

  private CompletionStage<LoginResult<?>> authenticate(
      AuthUser user, List<MfaMethod> userMfaMethods, @Nullable URI redirect) {
    UUID userId = user.getId();
    String organizationId = user.getOrganizationId();
    MDC.put(LoggingConstants.USER_ID, userId.toString());
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

    Supplier<CompletionStage<LoginResult<?>>> challengeSupplier =
        () ->
            mfaChallengeService
                .start(userId)
                .thenApply(
                    challengeId -> {
                      log.debug("[AUTH]: MFA challenge={} for user={}", challengeId, userId);
                      return new ChallengeLoginResult(challengeId, userMfaMethods, redirect);
                    });

    if (userMfaMethods.isEmpty()) {
      // TODO: should probably read from cache
      return organizationDatasource
          .retrieve(organizationId)
          .thenCompose(
              maybeOrganization -> {
                Organization organization =
                    maybeOrganization.orElseThrow(() -> Boom.notFound().exception());

                if (organization.isEnforceMultiFactorAuthentication()) {
                  log.debug("[AUTH]: User login with enforced MFA and no setup");
                  return challengeSupplier.get();
                }

                return authenticateDirect(user, redirect);
              });
    }

    return challengeSupplier.get();
  }

  @Override
  @Traced
  @Timed(name = "socialLogin", description = "A measure of how long it takes to do social login")
  public CompletionStage<LoginResult<?>> socialLogin(
      String email, String fullName, LoginMethod method, URI redirect, URI serverBaseUri) {
    MDC.put(LoggingConstants.USER_EMAIL, email);
    log.info(
        "[AUTH]: Social login attempt method={} email={} redirect={}", method, email, redirect);

    Function<Optional<SsoSetup>, CompletionStage<LoginResult<?>>> socialLoginProvider =
        (maybeSsoSetup) -> {
          CompletionStage<UserWithLoginInformation> signUpFuture =
              maybeSsoSetup.isEmpty()
                  ? socialFindOrSignUpUser(email, fullName)
                  : ssoFindOrSignUpUser(email, fullName, maybeSsoSetup.get().getOrganizationId());

          return signUpFuture
              .thenCompose(
                  userWithLoginInformation ->
                      authenticate(
                          userWithLoginInformation.user(),
                          userWithLoginInformation.getMfaMethods(),
                          redirect))
              .thenApply(
                  loginResult -> {
                    log.info(
                        "[AUTH]: Successful social login for user email={} method={}",
                        email,
                        method);
                    return loginResult;
                  });
        };

    Function<SsoSetup, CompletionStage<LoginResult<?>>> alternativeLoginProvider =
        ssoSetupDTO -> {
          SsoMethod ssoMethod = ssoSetupDTO.getMethod();
          log.info(
              "[AUTH]: Social login enforcing alternative login provider loginMethod={} ssoMethod={} redirect={}",
              method,
              ssoMethod,
              redirect);
          return CompletableFuture.completedStage(
              new ResponseLoginResult(
                  identityProviderRegistry.ssoSignInRedirectResponse(
                      ssoMethod, email, serverBaseUri, redirect)));
        };

    return login(email, method, alternativeLoginProvider, socialLoginProvider);
  }

  @Override
  public CompletionStage<LoginResult<?>> ssoLogin(
      String email, String fullName, String organizationId, URI redirect) {
    MDC.put(LoggingConstants.USER_EMAIL, email);
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    log.info(
        "[AUTH]: SSO login attempt email={} organizationId={} redirect={}",
        email,
        organizationId,
        redirect);

    return ssoFindOrSignUpUser(email, fullName, organizationId)
        .thenCompose(
            userWithLoginInformation -> {
              AuthUser user = userWithLoginInformation.user();
              List<MfaMethod> mfaMethods = userWithLoginInformation.getMfaMethods();
              return authenticate(user, mfaMethods, redirect);
            })
        .thenApply(
            loginResult -> {
              log.info("[AUTH]: Successful SSO login for user: {}", email);
              return loginResult;
            });
  }

  private CompletionStage<UserWithLoginInformation> ssoFindOrSignUpUser(
      String email, String fullName, String organizationId) {
    return userDatasource
        .findUserWithLoginInformation(email)
        .thenCompose(
            maybeUserWithLoginInformation -> {
              if (maybeUserWithLoginInformation.isPresent()) {
                UserWithLoginInformation user = maybeUserWithLoginInformation.get();
                log.info("[AUTH]: SSO linked with an existing user: {}", user.getId());
                return CompletableFuture.completedFuture(user);
              }

              return signUpService
                  .ssoSignUp(email, fullName, organizationId)
                  .thenApply(
                      authUser -> {
                        log.info("[AUTH]: User email={} signed up through SSO", email);
                        return UserWithLoginInformation.fresh(authUser);
                      });
            });
  }

  private CompletionStage<UserWithLoginInformation> socialFindOrSignUpUser(
      String email, String fullName) {
    return userDatasource
        .findUserWithLoginInformation(email)
        .thenCompose(
            maybeUserWithLoginInformation -> {
              if (maybeUserWithLoginInformation.isPresent()) {
                UserWithLoginInformation user = maybeUserWithLoginInformation.get();
                log.info("[AUTH]: Social login linked with an existing user: {}", user.getId());
                return CompletableFuture.completedFuture(user);
              }

              return signUpService
                  .socialSignUp(email, fullName)
                  .thenApply(
                      authUser -> {
                        log.info("[AUTH]: User email={} signed up through social sign in", email);
                        return UserWithLoginInformation.fresh(authUser);
                      });
            });
  }
}
