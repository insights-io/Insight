package com.meemaw.auth.utils;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.meemaw.auth.mfa.setup.resource.v1.MfaSetupResource;
import com.meemaw.auth.mfa.totp.datasource.MfaTotpSetupDatasource;
import com.meemaw.auth.mfa.totp.impl.TotpUtils;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.resource.v1.UserResource;
import com.meemaw.shared.SharedConstants;
import com.meemaw.shared.sms.MockSmsbox;
import com.meemaw.shared.sms.SmsMessage;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;

public final class AuthApiSetupUtils {

  private AuthApiSetupUtils() {}

  public static int getLastSmsMessageVerificationCode(
      MockSmsbox mockSmsbox, PhoneNumberDTO sentTo) {
    return getLastSmsMessageVerificationCode(mockSmsbox, sentTo.getNumber());
  }

  public static int getLastSmsMessageVerificationCode(MockSmsbox mockSmsbox, String sentTo) {
    List<SmsMessage> messages = mockSmsbox.getMessagesSentTo(sentTo);
    SmsMessage message = messages.get(messages.size() - 1);
    Pattern pattern =
        Pattern.compile(
            String.format(
                "^.*\\[%s\\] Verification code: (.*).*$", SharedConstants.ORGANIZATION_NAME));

    Matcher matcher = pattern.matcher(message.getBody());
    matcher.matches();
    return Integer.parseInt(matcher.group(1));
  }

  public static void setupSmsMfa(PhoneNumberDTO sentTo, String sessionId, MockSmsbox mockSmsbox)
      throws JsonProcessingException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(UserResource.PATH + "/phone_number/verify/send_code")
        .then()
        .statusCode(200);

    int phoneNumberVerificationCode = getLastSmsMessageVerificationCode(mockSmsbox, sentTo);
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            JacksonMapper.get()
                .writeValueAsString(new MfaChallengeCompleteDTO(phoneNumberVerificationCode)))
        .patch(UserResource.PATH + "/phone_number/verify")
        .then()
        .statusCode(200);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(MfaSetupResource.PATH + "/sms/start")
        .then()
        .statusCode(200);

    int code = getLastSmsMessageVerificationCode(mockSmsbox, sentTo);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(MfaSetupResource.PATH + "/sms/complete")
        .then()
        .statusCode(200);

    assertNotEquals(phoneNumberVerificationCode, code);
  }

  public static String setupTotpMfa(
      UUID userId, String sessionId, MfaTotpSetupDatasource mfaTotpSetupDatasource)
      throws GeneralSecurityException, JsonProcessingException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(MfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(200);

    String secret = mfaTotpSetupDatasource.retrieve(userId).toCompletableFuture().join().get();
    int code = TotpUtils.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(MfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(200);

    return secret;
  }
}
