package com.meemaw.auth.tfa.sms.impl;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

import com.meemaw.auth.tfa.AbstractTfaProvider;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.tfa.dto.TfaChallengeCodeDetailsDTO;
import com.meemaw.auth.tfa.model.TfaSetup;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTable.Errors;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.auth.user.phone.datasource.UserPhoneCodeDatasource;
import com.meemaw.auth.user.phone.service.UserPhoneCodeService;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class TfaSmsProvider extends AbstractTfaProvider<TfaChallengeCodeDetailsDTO> {

  @Inject UserPhoneCodeDatasource userPhoneCodeDatasource;
  @Inject UserTfaDatasource userTfaDatasource;
  @Inject UserDatasource userDatasource;
  @Inject SqlPool sqlPool;
  @Inject UserPhoneCodeService userPhoneCodeService;

  @Override
  public TfaMethod getMethod() {
    return TfaMethod.SMS;
  }

  @Override
  public CompletionStage<Boolean> completeChallenge(
      String challengeId, int code, TfaSetup tfaSetup) {
    String codeKey = challengeCodeKey(challengeId);
    return userPhoneCodeService
        .validate(code, codeKey)
        .thenApply(
            isValid -> {
              if (isValid) {
                userPhoneCodeDatasource.deleteCode(codeKey);
              }
              return isValid;
            });
  }

  @Override
  @Traced
  public CompletionStage<TfaSetup> setupComplete(UUID userId, int actualCode) {
    log.info("[AUTH]: Complete TFA SMS setup request for user={}", userId);

    String codeKey = setupCodeKey(userId);
    return userPhoneCodeService
        .validate(actualCode, codeKey)
        .thenCompose(
            isValid -> {
              if (!isValid) {
                log.debug("[AUTH]: TFA SMS setup complete code invalid for user={}", userId);
                throw Boom.badRequest().errors(TfaChallengeService.INVALID_CODE_ERRORS).exception();
              }

              return sqlPool
                  .beginTransaction()
                  .thenCompose(
                      transaction ->
                          userTfaDatasource
                              .storeSmsTfa(userId, transaction)
                              .thenCompose(
                                  tfaSetup ->
                                      transaction
                                          .commit()
                                          .thenApply(
                                              ignored -> {
                                                userPhoneCodeDatasource.deleteCode(codeKey);
                                                return tfaSetup;
                                              })))
                  .thenApply(
                      tfaSetup -> {
                        log.info("[AUTH]: TFA SMS setup complete successful for user={}", userId);
                        return tfaSetup;
                      });
            });
  }

  @Override
  @Traced
  public CompletionStage<TfaChallengeCodeDetailsDTO> setupStart(UUID userId, String email) {
    return userDatasource
        .findUser(userId)
        .thenCompose(
            maybeUser -> {
              if (maybeUser.isEmpty()) {
                throw Boom.notFound().exception();
              }

              AuthUser user = maybeUser.get();
              if (!user.isPhoneNumberVerified()) {
                throw Boom.badRequest()
                    .errors(Errors.PHONE_NUMBER_VERIFICATION_REQUIRED)
                    .exception();
              }

              return assertCanSetupTfa(userId)
                  .thenCompose(
                      i1 -> sendVerificationCode(setupCodeKey(userId), user.getPhoneNumber()));
            });
  }

  public CompletionStage<TfaChallengeCodeDetailsDTO> sendVerificationCode(
      String key, PhoneNumber phoneNumber) {
    return userPhoneCodeService
        .sendVerificationCode(key, phoneNumber)
        .thenApply(TfaChallengeCodeDetailsDTO::new);
  }

  // TODO: should be 3 separate caches
  // TODO: should be tied to sessionId
  public static String verifyCodeKey(UUID userId) {
    return String.format("%s-verify", userId);
  }

  // TODO: should be tied to sessionId
  public static String setupCodeKey(UUID userId) {
    return String.format("%s-setup", userId);
  }

  public static String challengeCodeKey(String challengeId) {
    return String.format("%s-challenge", challengeId);
  }
}
