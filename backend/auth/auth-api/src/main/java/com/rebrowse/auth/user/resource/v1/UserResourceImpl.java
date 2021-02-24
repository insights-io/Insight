package com.rebrowse.auth.user.resource.v1;

import com.rebrowse.auth.mfa.dto.MfaChallengeCodeDetailsDTO;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.mfa.sms.impl.MfaSmsProvider;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.user.datasource.UserTable;
import com.rebrowse.auth.user.datasource.UserTable.Errors;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.PhoneNumber;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.auth.user.phone.service.UserPhoneCodeService;
import com.rebrowse.auth.user.service.UserService;
import com.rebrowse.shared.rest.query.UpdateDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserResourceImpl implements UserResource {

  @Inject AuthPrincipal principal;
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
  public CompletionStage<Response> updatePhoneNumber(PhoneNumberDTO phoneNumber) {
    AuthUser user = principal.user();
    Map<String, Object> params = new LinkedHashMap<>(2);
    params.put(UserTable.PHONE_NUMBER, phoneNumber);
    return update(user.getId(), user, params);
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

  private CompletionStage<Response> update(
      UUID userId, AuthUser actor, Map<String, Object> params) {
    if (params.isEmpty()) {
      return CompletableFuture.completedStage(
          Boom.badRequest()
              .message("Validation Error")
              .errors(Map.of("body", "Required"))
              .response());
    }

    Map<String, String> errors = UpdateDTO.from(params).validate(UserTable.UPDATABLE_FIELDS);
    if (!errors.isEmpty()) {
      return CompletableFuture.completedStage(Boom.badRequest().errors(errors).response());
    }

    if (!userId.equals(actor.getId())) {
      return CompletableFuture.completedStage(Boom.notFound().response());
    }

    return userService.updateUser(actor, params).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> verifyPhoneNumber(MfaChallengeCompleteDTO body) {
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

    String codeKey = MfaSmsProvider.verifyCodeKey(principal);
    return userPhoneCodeService
        .sendVerificationCode(codeKey, phoneNumber)
        .thenApply(MfaChallengeCodeDetailsDTO::new)
        .thenApply(DataResponse::ok);
  }
}
