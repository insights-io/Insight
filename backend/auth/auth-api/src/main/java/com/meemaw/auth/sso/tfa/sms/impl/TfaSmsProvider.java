package com.meemaw.auth.sso.tfa.sms.impl;

import com.meemaw.auth.sso.tfa.AbstractTfaProvider;
import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.sso.tfa.setup.model.TfaSetup;
import com.meemaw.auth.sso.tfa.sms.model.dto.TfaSmsSetupStartDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class TfaSmsProvider extends AbstractTfaProvider<TfaSmsSetupStartDTO> {

  @Inject UserPhoneCodeDatasource userPhoneCodeDatasource;
  @Inject UserTfaDatasource userTfaDatasource;
  @Inject UserDatasource userDatasource;
  @Inject SqlPool sqlPool;
  @Inject UserPhoneCodeService userPhoneCodeService;

  @Override
  public CompletionStage<Boolean> validate(int actualCode, TfaSetup tfaSetup) {
    return userPhoneCodeService.validate(actualCode, tfaSetup.getUserId());
  }

  @Override
  @Traced
  public CompletionStage<TfaSetup> setupComplete(UUID userId, int actualCode) {
    log.info("[AUTH]: Complete TFA SMS setup request for user={}", userId);
    return userPhoneCodeService
        .validate(actualCode, userId)
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
                                    userPhoneCodeDatasource.deleteCode(userId);
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

              AuthUser user = maybeUser.get();
              if (!user.isPhoneNumberVerified()) {
                throw Boom.badRequest()
                    .errors(Errors.PHONE_NUMBER_VERIFICATION_REQUIRED)
                    .exception();
              }

              return assertCanSetupTfa(userId)
                  .thenCompose(i1 -> prepareChallenge(userId, user.getPhoneNumber()));
            });
  }

  @Traced
  public CompletionStage<TfaSmsSetupStartDTO> prepareChallenge(
      UUID userId, PhoneNumber phoneNumber) {
    log.info("[AUTH]: Preparing TFA SMS challenge for user={} phoneNumber={}", userId, phoneNumber);
    return userPhoneCodeService
        .sendVerificationCode(userId, phoneNumber)
        .thenApply(TfaSmsSetupStartDTO::new);
  }

  @Override
  public TfaMethod getMethod() {
    return TfaMethod.SMS;
  }
}
