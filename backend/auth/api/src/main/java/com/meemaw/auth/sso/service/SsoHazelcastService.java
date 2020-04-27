package com.meemaw.auth.sso.service;

import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.signup.service.SignupService;
import com.meemaw.auth.sso.datasource.SsoDatasource;
import com.meemaw.auth.sso.model.SsoUser;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.shared.auth.UserDTO;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class SsoHazelcastService implements SsoService {

  @Inject
  SsoDatasource ssoDatasource;

  @Inject
  PasswordService passwordService;

  @Inject
  UserDatasource userDatasource;

  @Inject
  SignupService signupService;

  @Override
  public CompletionStage<String> createSession(UserDTO user) {
    return ssoDatasource.createSession(user);
  }

  @Override
  public CompletionStage<Optional<UserDTO>> findSession(String sessionId) {
    return ssoDatasource.findSession(sessionId)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }

  @Override
  public CompletionStage<Boolean> logout(String sessionId) {
    return ssoDatasource.deleteSession(sessionId);
  }

  @Override
  public CompletionStage<String> login(String email, String password) {
    return passwordService.verifyPassword(email, password).thenCompose(this::createSession);
  }

  @Override
  public CompletionStage<String> socialLogin(String email) {
    return socialFindOrCreateUser(email).thenCompose(this::createSession);
  }

  private CompletionStage<UserDTO> socialFindOrCreateUser(String email) {
    return userDatasource.findUser(email)
        .thenCompose(maybeUser -> {
          if (maybeUser.isPresent()) {
            return CompletableFuture.completedFuture(maybeUser.get());
          }
          log.info("Social login new SSO user email={}", email);
          return signupService.createOrganization(email);
        });
  }

}
