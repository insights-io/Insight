package com.meemaw.auth.sso.service;

import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.signup.service.SignUpService;
import com.meemaw.auth.sso.datasource.SsoDatasource;
import com.meemaw.auth.sso.model.SsoUser;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class SsoServiceImpl implements SsoService {

  @Inject SsoDatasource ssoDatasource;
  @Inject PasswordService passwordService;
  @Inject UserDatasource userDatasource;
  @Inject SignUpService signUpService;

  @Override
  @Traced
  @Timed(name = "createSession", description = "A measure of how long it takes to create session")
  public CompletionStage<String> createSession(AuthUser user) {
    return ssoDatasource.createSession(user);
  }

  @Override
  @Traced
  @Timed(name = "findSession", description = "A measure of how long it takes to do logout")
  public CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
    return ssoDatasource
        .findSession(sessionId)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }

  @Override
  @Traced
  @Timed(name = "findSessions", description = "A measure of how long it takes to find SSO sessions")
  public CompletionStage<Set<String>> findSessions(String sessionId) {
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
    return ssoDatasource.deleteSession(sessionId);
  }

  @Override
  @Traced
  @Timed(
      name = "logoutFromAllDevices",
      description = "A measure of how long it takes to do a logout from all devices")
  public CompletionStage<Set<String>> logoutUserFromAllDevices(UUID userId) {
    return ssoDatasource.deleteAllSessionsForUser(userId);
  }

  @Override
  @Traced
  @Timed(name = "login", description = "A measure of how long it takes to do login")
  public CompletionStage<String> login(String email, String password) {
    return passwordService.verifyPassword(email, password).thenCompose(this::createSession);
  }

  @Override
  @Traced
  @Timed(name = "socialLogin", description = "A measure of how long it takes to do social login")
  public CompletionStage<String> socialLogin(String email, String fullName) {
    return socialFindOrSignUpUser(email, fullName).thenCompose(this::createSession);
  }

  private CompletionStage<AuthUser> socialFindOrSignUpUser(String email, String fullName) {
    return userDatasource
        .findUser(email)
        .thenCompose(
            maybeUser -> {
              if (maybeUser.isPresent()) {
                return CompletableFuture.completedFuture(maybeUser.get());
              }
              log.info("Creating new user for social sign in");
              return signUpService.socialSignUp(email, fullName);
            });
  }
}
