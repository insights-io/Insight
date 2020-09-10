package com.meemaw.auth.sso.session.service;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.signup.service.SignUpService;
import com.meemaw.auth.sso.IdpServiceRegistry;
import com.meemaw.auth.sso.session.datasource.SsoDatasource;
import com.meemaw.auth.sso.session.model.DirectLoginResult;
import com.meemaw.auth.sso.session.model.LoginResult;
import com.meemaw.auth.sso.session.model.RedirectSessionLoginResult;
import com.meemaw.auth.sso.session.model.SsoUser;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.challenge.model.ChallengeLoginResult;
import com.meemaw.auth.sso.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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

  @Inject SsoDatasource ssoDatasource;
  @Inject PasswordService passwordService;
  @Inject UserDatasource userDatasource;
  @Inject SignUpService signUpService;
  @Inject TfaChallengeService tfaChallengeService;
  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject IdpServiceRegistry idpServiceRegistry;

  @Override
  @Traced
  @Timed(
      name = "createSessionForUser",
      description = "A measure of how long it takes to create session for user")
  public CompletionStage<String> createSession(AuthUser user) {
    MDC.put(LoggingConstants.USER_ID, user.getId().toString());
    MDC.put(LoggingConstants.USER_EMAIL, user.getEmail());
    MDC.put(LoggingConstants.ORGANIZATION_ID, user.getOrganizationId());
    return ssoDatasource
        .createSession(user)
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
    return ssoDatasource
        .findSession(sessionId)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }

  @Override
  @Traced
  @Timed(name = "findSessions", description = "A measure of how long it takes to find SSO sessions")
  public CompletionStage<Set<String>> findSessions(String sessionId) {
    MDC.put(LoggingConstants.SSO_SESSION_ID, sessionId);
    log.info("[AUTH]: Find all sessions related to session: {}", sessionId);
    return ssoDatasource
        .findSession(sessionId)
        .thenCompose(
            ssoUser ->
                ssoDatasource.getAllSessionsForUser(
                    ssoUser.orElseThrow(() -> Boom.notFound().exception()).getId()));
  }

  @Override
  @Traced
  @Timed(name = "logout", description = "A measure of how long it takes to do logout")
  public CompletionStage<Optional<SsoUser>> logout(String sessionId) {
    MDC.put(LoggingConstants.SSO_SESSION_ID, sessionId);
    log.info("[AUTH]: Logout attempt for session: {}", sessionId);
    return ssoDatasource.deleteSession(sessionId);
  }

  @Override
  @Traced
  @Timed(
      name = "logoutFromAllDevices",
      description = "A measure of how long it takes to do a logout from all devices")
  public CompletionStage<Set<String>> logoutUserFromAllDevices(UUID userId) {
    MDC.put(LoggingConstants.USER_ID, userId.toString());
    log.info("[AUTH]: Logout from all devices userId: {}", userId);
    return ssoDatasource
        .deleteAllSessionsForUser(userId)
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
      String email, String password, String ipAddress, String serverBaseURL, String redirect) {
    MDC.put(LoggingConstants.USER_EMAIL, email);

    Supplier<CompletionStage<LoginResult<?>>> passwordLoginSupplier =
        () -> {
          log.info("[AUTH]: Email login attempt with password email={} ip={}", email, ipAddress);
          return passwordService
              .verifyPassword(email, password)
              .thenCompose(
                  userWithLoginInformation ->
                      authenticate(
                          userWithLoginInformation.user(),
                          userWithLoginInformation.getTfaMethods()));
        };

    if (EmailUtils.isBusinessDomain(email)) {
      log.info("[AUTH]: Login attempt with business email={} ip={}", email, ipAddress);
      return ssoSetupDatasource
          .getByDomain(EmailUtils.domainFromEmail(email))
          .thenCompose(
              maybeSsoSetup -> {
                if (maybeSsoSetup.isEmpty()) {
                  return passwordLoginSupplier.get();
                }

                String ssoSignInLocation =
                    idpServiceRegistry.signInLocation(
                        serverBaseURL, maybeSsoSetup.get().getMethod(), redirect);

                log.info(
                    "[AUTH]: SSO login required email={} ssoSignInLocation={}",
                    email,
                    ssoSignInLocation);

                throw Boom.badRequest()
                    .message("SSO login required")
                    .errors(Map.of("goto", ssoSignInLocation))
                    .exception();
              });
    }

    return passwordLoginSupplier.get();
  }

  private CompletionStage<LoginResult<?>> authenticate(AuthUser user, List<TfaMethod> tfaMethods) {
    return authenticate(user, tfaMethods, null);
  }

  private CompletionStage<LoginResult<?>> authenticate(
      AuthUser user, List<TfaMethod> tfaMethods, @Nullable String clientCallbackRedirect) {
    UUID userId = user.getId();
    String organizationId = user.getOrganizationId();
    MDC.put(LoggingConstants.USER_ID, userId.toString());
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

    if (tfaMethods.isEmpty()) {
      return this.createSession(user)
          .thenApply(
              sessionId -> {
                if (clientCallbackRedirect != null) {
                  log.info(
                      "[AUTH]: Successful login for user={} clientCallbackRedirect={}",
                      userId,
                      clientCallbackRedirect);
                  return new RedirectSessionLoginResult(sessionId, clientCallbackRedirect);
                }
                log.info("[AUTH]: Successful login for user={}", userId);
                return new DirectLoginResult(sessionId);
              });
    }

    return tfaChallengeService
        .start(userId)
        .thenApply(
            challengeId -> {
              log.info("[AUTH]: TFA challenge={} for user={}", challengeId, userId);
              return new ChallengeLoginResult(challengeId, tfaMethods, clientCallbackRedirect);
            });
  }

  @Override
  @Traced
  @Timed(name = "socialLogin", description = "A measure of how long it takes to do social login")
  public CompletionStage<LoginResult<?>> socialLogin(
      String email, String fullName, String clientCallbackRedirect) {
    MDC.put(LoggingConstants.USER_EMAIL, email);
    log.info(
        "[AUTH]: Social login attempt email={} clientCallbackRedirect={}",
        email,
        clientCallbackRedirect);

    return socialFindOrSignUpUser(email, fullName)
        .thenCompose(
            userWithLoginInformation ->
                authenticate(
                    userWithLoginInformation.user(),
                    userWithLoginInformation.getTfaMethods(),
                    clientCallbackRedirect))
        .thenApply(
            loginResult -> {
              log.info("[AUTH]: Successful social login for user: {}", email);
              return loginResult;
            });
  }

  @Override
  public CompletionStage<LoginResult<?>> ssoLogin(
      String email, String fullName, String organizationId, String clientCallbackRedirect) {
    MDC.put(LoggingConstants.USER_EMAIL, email);
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    log.info(
        "[AUTH]: SSO login attempt email={} organizationId={} clientCallbackRedirect={}",
        email,
        organizationId,
        clientCallbackRedirect);

    return ssoFindOrSignUpUser(email, fullName, organizationId)
        .thenCompose(
            userWithLoginInformation -> {
              AuthUser user = userWithLoginInformation.user();
              List<TfaMethod> tfaMethods = userWithLoginInformation.getTfaMethods();
              return authenticate(user, tfaMethods, clientCallbackRedirect);
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
