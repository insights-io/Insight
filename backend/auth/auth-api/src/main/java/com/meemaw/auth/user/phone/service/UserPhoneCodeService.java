package com.meemaw.auth.user.phone.service;

import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.auth.user.phone.datasource.UserPhoneCodeDatasource;
import com.meemaw.shared.sms.SmsMessage;
import com.meemaw.shared.sms.SmsService;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class UserPhoneCodeService {

  @Inject UserPhoneCodeDatasource userPhoneCodeDatasource;
  @Inject SmsService smsService;

  public CompletionStage<Boolean> validate(int actualCode, UUID userId) {
    return userPhoneCodeDatasource
        .getCode(userId)
        .thenApply(
            maybeCode -> {
              if (maybeCode.isEmpty()) {
                return false;
              }
              return maybeCode.get() == actualCode;
            });
  }

  @Traced
  public CompletionStage<Integer> sendVerificationCode(UUID userId, PhoneNumber phoneNumber) {
    int code = newCode();
    return userPhoneCodeDatasource
        .setCode(userId, code)
        .thenCompose(
            validitySeconds ->
                sendVerificationCode(code, phoneNumber).thenApply(i1 -> validitySeconds));
  }

  @Traced
  private CompletionStage<SmsMessage> sendVerificationCode(int code, PhoneNumber phoneNumber) {
    return smsService.sendMessage(
        "+19704594909",
        phoneNumber.getNumber(),
        String.format("[Insight] Verification code: %d", code));
  }

  private int newCode() {
    return Integer.parseInt(RandomStringUtils.randomNumeric(6));
  }
}
