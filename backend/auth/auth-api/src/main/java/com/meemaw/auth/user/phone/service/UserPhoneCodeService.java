package com.meemaw.auth.user.phone.service;

import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.auth.user.phone.datasource.UserPhoneCodeDatasource;
import com.meemaw.shared.SharedConstants;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sms.SmsMessage;
import com.meemaw.shared.sms.SmsService;
import java.security.SecureRandom;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class UserPhoneCodeService {

  public static final int CODE_LENGTH = 6;
  private static final SecureRandom random = new SecureRandom();

  @Inject UserPhoneCodeDatasource userPhoneCodeDatasource;
  @Inject SmsService smsService;

  public static int generateRandomDigits(int length) {
    int min = (int) Math.pow(10, length - 1);
    return min + random.nextInt(9 * min);
  }

  public CompletionStage<Boolean> validate(int actualCode, String key) {
    return userPhoneCodeDatasource
        .getCode(key)
        .thenApply(
            maybeCode -> {
              if (maybeCode.isEmpty()) {
                throw Boom.notFound().message("Code expired").exception();
              }

              return maybeCode.get() == actualCode;
            });
  }

  @Traced
  public CompletionStage<Integer> sendVerificationCode(String key, PhoneNumber phoneNumber) {
    int code = newCode();
    return userPhoneCodeDatasource
        .setCode(key, code)
        .thenCompose(
            validitySeconds ->
                sendVerificationCode(code, phoneNumber).thenApply(i1 -> validitySeconds));
  }

  @Traced
  private CompletionStage<SmsMessage> sendVerificationCode(int code, PhoneNumber phoneNumber) {
    return smsService.sendMessage(
        "+19704594909",
        phoneNumber.getNumber(),
        String.format("[%s] Verification code: %d", SharedConstants.ORGANIZATION_NAME, code));
  }

  public int newCode() {
    return generateRandomDigits(CODE_LENGTH);
  }
}
