package com.meemaw.auth.sso.service;

import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.signup.service.SignUpServiceImpl;
import com.meemaw.auth.sso.datasource.SsoDatasource;
import com.meemaw.auth.sso.model.SsoUser;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;

@ApplicationScoped
@Slf4j
public class SsoHazelcastService implements SsoService {

  @Inject SsoDatasource ssoDatasource;
  @Inject PasswordService passwordService;
  @Inject UserDatasource userDatasource;
  @Inject SignUpServiceImpl signUpService;

  @Override
  @Timed(name = "createSession", description = "A measure of how long it takes to create session")
  public CompletionStage<String> createSession(AuthUser user) {
    return ssoDatasource.createSession(user);
  }

  @Override
  @Timed(name = "findSession", description = "A measure of how long it takes to do logout")
  public CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
    return ssoDatasource
        .findSession(sessionId)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }

  @Override
  @Timed(name = "logout", description = "A measure of how long it takes to do logout")
  public CompletionStage<Boolean> logout(String sessionId) {
    return ssoDatasource.deleteSession(sessionId);
  }

  @Override
  @Timed(name = "login", description = "A measure of how long it takes to do login")
  public CompletionStage<String> login(String email, String password) {
    return passwordService.verifyPassword(email, password).thenCompose(this::createSession);
  }

  @Override
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
