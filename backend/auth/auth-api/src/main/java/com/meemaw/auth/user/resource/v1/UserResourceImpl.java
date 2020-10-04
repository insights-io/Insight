package com.meemaw.auth.user.resource.v1;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.sms.model.dto.TfaSmsSetupStartDTO;
import com.meemaw.auth.user.datasource.UserTable;
import com.meemaw.auth.user.datasource.UserTable.Errors;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.auth.user.phone.service.UserPhoneCodeService;
import com.meemaw.auth.user.service.UserService;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserResourceImpl implements UserResource {

  @Inject InsightPrincipal principal;
  @Inject UserService userService;
  @Inject UserPhoneCodeService userPhoneCodeService;

  @Override
  public CompletionStage<Response> retrieveAssociated(String sessionId) {
    return CompletableFuture.completedStage(DataResponse.ok(principal.user()));
  }

  @Override
  public CompletionStage<Response> retrieve(UUID userId) {
    AuthUser user = principal.user();
    if (!user.getId().equals(userId)) {
      throw Boom.notFound().exception();
    }

    return userService
        .getUser(userId)
        .thenApply(
            maybeUser -> {
              if (maybeUser.isEmpty()) {
                throw Boom.notFound().exception();
              }
              return DataResponse.ok(maybeUser.get());
            });
  }

  @Override
  public CompletionStage<Response> update(Map<String, Object> body) {
    AuthUser user = principal.user();
    return update(user.getId(), user, body);
  }

  @Override
  public CompletionStage<Response> update(UUID userId, Map<String, Object> body) {
    return update(userId, principal.user(), body);
  }

  private CompletionStage<Response> update(UUID userId, AuthUser actor, Map<String, Object> body) {
    if (body.isEmpty()) {
      return CompletableFuture.completedStage(
          Boom.badRequest()
              .message("Validation Error")
              .errors(Map.of("body", "Required"))
              .response());
    }

    Map<String, String> errors = new HashMap<>();
    for (Entry<String, ?> entry : body.entrySet()) {
      if (!UserTable.UPDATABLE_FIELDS.contains(entry.getKey())) {
        errors.put(entry.getKey(), "Unexpected field");
      }
    }

    if (!errors.isEmpty()) {
      return CompletableFuture.completedStage(Boom.badRequest().errors(errors).response());
    }

    if (!userId.equals(actor.getId())) {
      return CompletableFuture.completedStage(Boom.notFound().response());
    }

    return userService.updateUser(actor, body).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> verifyPhoneNumber(TfaChallengeCompleteDTO body) {
    AuthUser user = principal.user();
    return userService.verifyPhoneNumber(user, body.getCode()).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> phoneNumberVerifySendCode() {
    AuthUser user = principal.user();
    UUID userId = user.getId();
    if (user.isPhoneNumberVerified()) {
      log.info(
          "[AUTH]: Tried to send phone number verify code to user={} with already verified phone number",
          userId);
      return CompletableFuture.completedStage(Boom.badRequest().response());
    }

    PhoneNumber phoneNumber = user.getPhoneNumber();
    if (phoneNumber == null) {
      log.info(
          "[AUTH]: Tried to send phone number verify code to user={} with no phone number configured",
          userId);

      return CompletableFuture.completedStage(
          Boom.badRequest().errors(Errors.PHONE_NUMBER_REQUIRED).response());
    }

    log.info(
        "[AUTH]: Sending phone number verification code user={} phoneNumber={}",
        userId,
        phoneNumber);

    return userPhoneCodeService
        .sendVerificationCode(userId, phoneNumber)
        .thenApply(TfaSmsSetupStartDTO::new)
        .thenApply(DataResponse::ok);
  }
}
