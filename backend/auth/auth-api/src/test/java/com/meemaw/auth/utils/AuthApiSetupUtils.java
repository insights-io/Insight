package com.meemaw.auth.utils;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.tfa.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.setup.resource.v1.TfaSetupResource;
import com.meemaw.auth.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.tfa.totp.impl.TotpUtils;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.resource.v1.UserResource;
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
    List<SmsMessage> messages = mockSmsbox.getMessagesSentTo(sentTo.getNumber());
    SmsMessage message = messages.get(messages.size() - 1);
    Pattern pattern = Pattern.compile("^.*\\[Insight\\] Verification code: (.*).*$");
    Matcher matcher = pattern.matcher(message.getBody());
    matcher.matches();
    return Integer.parseInt(matcher.group(1));
  }

  public static void setupSmsTfa(PhoneNumberDTO sentTo, String sessionId, MockSmsbox mockSmsbox)
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
                .writeValueAsString(new TfaChallengeCompleteDTO(phoneNumberVerificationCode)))
        .patch(UserResource.PATH + "/phone_number/verify")
        .then()
        .statusCode(200);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(TfaSetupResource.PATH + "/sms/start")
        .then()
        .statusCode(200);

    int tfaSetupCode = getLastSmsMessageVerificationCode(mockSmsbox, sentTo);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaSetupCode)))
        .post(TfaSetupResource.PATH + "/sms/complete")
        .then()
        .statusCode(200);

    assertNotEquals(phoneNumberVerificationCode, tfaSetupCode);
  }

  public static String setupTotpTfa(
      UUID userId, String sessionId, TfaTotpSetupDatasource tfaTotpSetupDatasource)
      throws GeneralSecurityException, JsonProcessingException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(TfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(200);

    String secret = tfaTotpSetupDatasource.getTotpSecret(userId).toCompletableFuture().join().get();
    int tfaCode = TotpUtils.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
        .post(TfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(200);

    return secret;
  }
}
