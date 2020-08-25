package com.meemaw.auth.tfa.sms.impl;

import com.meemaw.auth.tfa.AbstractTfaProvider;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.tfa.setup.model.TfaSetup;
import com.meemaw.auth.tfa.sms.datasource.TfaSmsDatasource;
import com.meemaw.auth.tfa.sms.model.dto.TfaSmsSetupStartDTO;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sms.SmsMessage;
import com.meemaw.shared.sms.SmsService;
import com.meemaw.shared.sql.client.SqlPool;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class TfaSmsProvider extends AbstractTfaProvider<TfaSmsSetupStartDTO> {

  @Inject TfaSmsDatasource tfaSmsDatasource;
  @Inject UserTfaDatasource userTfaDatasource;
  @Inject UserDatasource userDatasource;
  @Inject SmsService smsService;
  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<Boolean> validate(int actualCode, TfaSetup tfaSetup) {
    return validate(actualCode, tfaSetup.getUserId());
  }

  @Traced
  private CompletionStage<Boolean> validate(int actualCode, UUID userId) {
    log.debug("[AUTH]: Validating TFA SMS code={} for user={}", actualCode, userId);
    return tfaSmsDatasource
        .getCode(userId)
        .thenApply(
            maybeCode -> {
              if (maybeCode.isEmpty()) {
                log.info("[AUTH]: Tried to validate TFA SMS, but code is missing user={}", userId);
                return false;
              }

              return maybeCode.get() == actualCode;
            });
  }

  @Override
  @Traced
  public CompletionStage<TfaSetup> setupComplete(UUID userId, int actualCode) {
    return validate(actualCode, userId)
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
                                  tfaSetup -> {
                                    tfaSmsDatasource.deleteCode(userId);
                                    return transaction.commit().thenApply(k -> tfaSetup);
                                  }))
                  .thenApply(
                      tfaSetup -> {
                        log.info("[AUTH]: TFA SMS setup complete successful for user={}", userId);
                        return tfaSetup;
                      });
            });
  }

  @Override
  @Traced
  public CompletionStage<TfaSmsSetupStartDTO> setupStart(UUID userId, String email) {
    return userDatasource
        .findUser(userId)
        .thenCompose(
            maybeUser -> {
              if (maybeUser.isEmpty()) {
                throw Boom.notFound().exception();
              }

              // TODO: validate that user has phone number verified
              String phoneNumber = maybeUser.get().getPhoneNumber();
              return assertCanSetupTfa(userId)
                  .thenCompose(i1 -> prepareChallenge(userId, phoneNumber));
            });
  }

  @Traced
  public CompletionStage<TfaSmsSetupStartDTO> prepareChallenge(UUID userId, String phoneNumber) {
    int code = newCode();
    log.info(
        "[AUTH]: Preparing SMS challenge code={} for user={} phoneNumber={}",
        code,
        userId,
        phoneNumber);

    return tfaSmsDatasource
        .setCode(userId, code)
        .thenCompose(
            validitySeconds ->
                sendVerificationCode(code, phoneNumber)
                    .thenApply(i1 -> new TfaSmsSetupStartDTO(validitySeconds)));
  }

  @Traced
  public CompletionStage<SmsMessage> sendVerificationCode(int code, String to) {
    return smsService.sendMessage(
        "+19704594909", to, String.format("[Insight] Verification code: %d", code));
  }

  private int newCode() {
    return Integer.parseInt(RandomStringUtils.randomNumeric(6));
  }

  @Override
  public TfaMethod getMethod() {
    return TfaMethod.SMS;
  }
}
