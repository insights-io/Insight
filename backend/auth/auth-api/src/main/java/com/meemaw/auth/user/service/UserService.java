package com.meemaw.auth.user.service;

import com.meemaw.auth.mfa.challenge.service.MfaChallengeService;
import com.meemaw.auth.mfa.sms.impl.MfaSmsProvider;
import com.meemaw.auth.sso.session.datasource.SsoSessionDatasource;
import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTable;
import com.meemaw.auth.user.datasource.UserTable.Errors;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.phone.datasource.UserPhoneCodeDatasource;
import com.meemaw.auth.user.phone.service.UserPhoneCodeService;
import com.meemaw.shared.rest.query.UpdateDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
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
  @Inject UserPhoneCodeDatasource userPhoneCodeDatasource;
  @Inject AuthPrincipal principal;

  @Traced
  public CompletionStage<Optional<AuthUser>> getUser(UUID userId) {
    return userDatasource.retrieve(userId);
  }

  @Traced
  public CompletionStage<AuthUser> updateUser(AuthUser user, Map<String, Object> params) {
    UUID userId = user.getId();

    if (params.containsKey(UserTable.PHONE_NUMBER)) {
      Object maybePhoneNumber = params.get(UserTable.PHONE_NUMBER);
      if (maybePhoneNumber == null) {
        if (user.getPhoneNumber() != null) {
          params.put(UserTable.PHONE_NUMBER_VERIFIED, false);
          log.debug(
              "[AUTH]: Phone number change for user={} -- set phone_number_verified=false", userId);
        }
      } else {
        JsonObject phoneNumber = JsonObject.mapFrom(maybePhoneNumber);
        if (!phoneNumber.mapTo(PhoneNumberDTO.class).equals(user.getPhoneNumber())) {
          // TODO: this should probably be done using a DB trigger
          params.put(UserTable.PHONE_NUMBER_VERIFIED, false);
          params.put(UserTable.PHONE_NUMBER, phoneNumber);
          log.debug(
              "[AUTH]: Phone number change for user={} -- set phone_number_verified=false", userId);
        }
      }
    }

    return updateUser(userId, UpdateDTO.from(params));
  }

  private CompletionStage<AuthUser> updateUser(Supplier<CompletionStage<AuthUser>> update) {
    return update
        .get()
        .thenCompose(
            updatedUser ->
                ssoSessionDatasource
                    .updateAllForUser(updatedUser.getId(), updatedUser)
                    .thenApply(i1 -> updatedUser));
  }

  public CompletionStage<AuthUser> updateUser(UUID userId, UpdateDTO params) {
    return updateUser(() -> userDatasource.update(userId, params));
  }

  public CompletionStage<AuthUser> updateUser(
      UUID userId, UpdateDTO params, SqlTransaction transaction) {
    return updateUser(() -> userDatasource.update(userId, params, transaction));
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
    String key = MfaSmsProvider.verifyCodeKey(principal);
    return userPhoneCodeService
        .validate(code, key)
        .thenCompose(
            isValid -> {
              if (!isValid) {
                log.info(
                    "[AUTH]: Failed to verify phone number for user={} due to invalid code",
                    userId);
                throw Boom.badRequest().errors(MfaChallengeService.INVALID_CODE_ERRORS).exception();
              }

              UpdateDTO updates = UpdateDTO.from(Map.of(UserTable.PHONE_NUMBER_VERIFIED, true));
              return updateUser(userId, updates)
                  .thenApply(
                      updatedUser -> {
                        userPhoneCodeDatasource.deleteCode(key);
                        return updatedUser;
                      });
            });
  }
}
