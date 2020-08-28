package com.meemaw.auth.user.service;

import com.meemaw.auth.sso.datasource.SsoDatasource;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class UserService {

  @Inject UserDatasource userDatasource;
  @Inject SsoDatasource ssoDatasource;

  public CompletionStage<AuthUser> updateUser(UUID userId, Map<String, ?> body) {
    log.info("[AUTH]: Update user={} request body={}", userId, body);
    return userDatasource
        .updateUser(userId, body)
        .thenCompose(
            updatedUser ->
                ssoDatasource.updateUserSessions(userId, updatedUser).thenApply(i1 -> updatedUser));
  }
}
