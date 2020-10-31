package com.meemaw.auth.user.service;

import com.meemaw.auth.sso.session.datasource.SsoSessionDatasource;
import com.meemaw.auth.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTable;
import com.meemaw.auth.user.datasource.UserTable.Errors;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.phone.service.UserPhoneCodeService;
import com.meemaw.shared.rest.query.UpdateDTO;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.json.JsonObject;
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
public class UserService {

  @Inject UserDatasource userDatasource;
  @Inject SsoSessionDatasource ssoSessionDatasource;
  @Inject UserPhoneCodeService userPhoneCodeService;

  @Traced
  public CompletionStage<Optional<AuthUser>> getUser(UUID userId) {
    return userDatasource.findUser(userId);
  }

  @Traced
  public CompletionStage<AuthUser> updateUser(AuthUser user, Map<String, Object> params) {
    UUID userId = user.getId();

    if (params.containsKey(UserTable.PHONE_NUMBER)) {
      JsonObject phoneNumber = JsonObject.mapFrom(params.get(UserTable.PHONE_NUMBER));
      if (!phoneNumber.mapTo(PhoneNumberDTO.class).equals(user.getPhoneNumber())) {
        // TODO: this should probably be done using a DB trigger
        params.put(UserTable.PHONE_NUMBER_VERIFIED, false);
        params.put(UserTable.PHONE_NUMBER, phoneNumber);
        log.info(
            "[AUTH]: Phone number change for user={} -- set phone_number_verified=false", userId);
      }
    }

    return updateUser(userId, UpdateDTO.from(params));
  }

  private CompletionStage<AuthUser> updateUser(UUID userId, UpdateDTO params) {
    return userDatasource
        .updateUser(userId, params)
        .thenCompose(
            updatedUser ->
                ssoSessionDatasource
                    .updateAllForUser(userId, updatedUser)
                    .thenApply(i1 -> updatedUser));
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

              return updateUser(
                  userId, UpdateDTO.from(Map.of(UserTable.PHONE_NUMBER_VERIFIED, true)));
            });
  }
}
