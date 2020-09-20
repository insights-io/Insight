package com.meemaw.auth.user.service;

import com.meemaw.auth.sso.session.datasource.SsoDatasource;
import com.meemaw.auth.sso.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTable;
import com.meemaw.auth.user.datasource.UserTable.Errors;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.phone.service.UserPhoneCodeService;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.json.JsonObject;
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

    if (body.containsKey(UserTable.PHONE_NUMBER)) {
      JsonObject phoneNumber = JsonObject.mapFrom(body.get(UserTable.PHONE_NUMBER));
      if (!phoneNumber.mapTo(PhoneNumberDTO.class).equals(user.getPhoneNumber())) {
        // TODO: this should probably be done using a DB trigger
        body.put(UserTable.PHONE_NUMBER_VERIFIED, false);
        body.put(UserTable.PHONE_NUMBER, phoneNumber);
        log.info(
            "[AUTH]: Phone number change for user={} -- set phone_number_verified=false", userId);
      }
    }

    return updateUser(userId, body);
  }

  private CompletionStage<AuthUser> updateUser(UUID userId, Map<String, Object> body) {
    return userDatasource
        .updateUser(userId, body)
        .thenCompose(
            updatedUser ->
                ssoDatasource.updateUserSessions(userId, updatedUser).thenApply(i1 -> updatedUser));
  }

  @Traced
  public CompletionStage<AuthUser> verifyPhoneNumber(AuthUser user, int code) {
    UUID userId = user.getId();
    if (user.getPhoneNumber() == null) {
      log.info(
          "[AUTH]: Tried to verify phone number for user={} that has not phone number", userId);
      throw Boom.badRequest().errors(Errors.PHONE_NUMBER_REQUIRED).exception();
    }

    if (user.isPhoneNumberVerified()) {
      log.info("[AUTH]: Tried to verify phone number for user={} that is already verified", userId);
      return CompletableFuture.completedStage(user);
    }

    log.info("[AUTH]: Trying to verify phone number user={} code={}", userId, code);
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
}
