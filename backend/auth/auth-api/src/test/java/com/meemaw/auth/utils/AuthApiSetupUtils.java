package com.meemaw.auth.utils;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.setup.resource.v1.TfaResource;
import com.meemaw.auth.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.tfa.totp.impl.TotpUtils;
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

  public static int getLastSmsMessageVerificationCode(MockSmsbox mockSmsbox, String sentTo) {
    List<SmsMessage> messages = mockSmsbox.getMessagesSentTo(sentTo);
    SmsMessage message = messages.get(messages.size() - 1);
    Pattern pattern = Pattern.compile("^.*\\[Insight\\] Verification code: (.*).*$");
    Matcher matcher = pattern.matcher(message.getBody());
    matcher.matches();
    return Integer.parseInt(matcher.group(1));
  }

  public static void setupSmsTfa(String sentTo, String sessionId, MockSmsbox mockSmsbox)
      throws JsonProcessingException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaResource.PATH + "/sms/setup")
        .then()
        .statusCode(200);

    int code = getLastSmsMessageVerificationCode(mockSmsbox, sentTo);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(code)))
        .post(TfaResource.PATH + "/sms/setup")
        .then()
        .statusCode(200);
  }

  public static String setupTotpTfa(
      UUID userId, String sessionId, TfaTotpSetupDatasource tfaTotpSetupDatasource)
      throws GeneralSecurityException, JsonProcessingException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(200);

    String secret = tfaTotpSetupDatasource.getTotpSecret(userId).toCompletableFuture().join().get();
    int tfaCode = TotpUtils.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
        .post(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(200);

    return secret;
  }
}
