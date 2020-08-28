package com.meemaw.auth.user.service;

import com.meemaw.auth.sso.datasource.SsoDatasource;
import com.meemaw.auth.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTable;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.phone.service.UserPhoneCodeService;
import com.meemaw.shared.rest.response.Boom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class UserService {

  @Inject UserDatasource userDatasource;
  @Inject SsoDatasource ssoDatasource;
  @Inject UserPhoneCodeService userPhoneCodeService;

  @Traced
  public CompletionStage<AuthUser> updateUser(AuthUser user, Map<String, Object> body) {
    UUID userId = user.getId();
    log.info("[AUTH]: Update user={} request body={}", userId, body);

    // TODO: this should probably be done using a DB trigger
    if (body.containsKey(UserTable.PHONE_NUMBER)
        && !body.get(UserTable.PHONE_NUMBER).equals(user.getPhoneNumber())) {
      body.put(UserTable.PHONE_NUMBER_VERIFIED, false);
      log.info(
          "[AUTH]: Phone number change for user={} -- set phone_number_verified=false", userId);
    }

    return updateUser(userId, body);
  }

  @Traced
  public CompletionStage<AuthUser> verifyPhoneNumber(AuthUser user, int code) {
    UUID userId = user.getId();
    if (user.isPhoneNumberVerified()) {
      log.info("[AUTH]: Tried to verify phone number for user={} that is already verified", userId);
      return CompletableFuture.completedStage(user);
    }

    return userPhoneCodeService
        .validate(code, userId)
        .thenCompose(
            isValid -> {
              if (!isValid) {
                log.info(
                    "[AUTH]: Failed to verify phone number for user={} due to invalid code",
                    userId);
                throw Boom.badRequest().errors(TfaChallengeService.INVALID_CODE_ERRORS).exception();
              }

              return updateUser(userId, Map.of(UserTable.PHONE_NUMBER_VERIFIED, true));
            });
  }

  private CompletionStage<AuthUser> updateUser(UUID userId, Map<String, Object> body) {
    return userDatasource
        .updateUser(userId, body)
        .thenCompose(
            updatedUser ->
                ssoDatasource.updateUserSessions(userId, updatedUser).thenApply(i1 -> updatedUser));
  }
}
