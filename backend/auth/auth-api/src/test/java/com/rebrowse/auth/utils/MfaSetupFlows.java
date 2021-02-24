package com.rebrowse.auth.utils;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.NotFoundException;
import com.rebrowse.auth.mfa.dto.MfaChallengeCodeDetailsDTO;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.mfa.model.dto.MfaSetupDTO;
import com.rebrowse.auth.mfa.setup.resource.v1.MfaSetupResource;
import com.rebrowse.auth.mfa.totp.impl.QRCodeUtils;
import com.rebrowse.auth.mfa.totp.impl.TotpUtils;
import com.rebrowse.auth.mfa.totp.model.dto.MfaTotpSetupStartDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.model.user.User;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.shared.sms.MockSmsbox;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import io.restassured.common.mapper.TypeRef;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class MfaSetupFlows extends AbstractTestFlow {

  private final MockSmsbox smsBox;
  private final String issuer;
  private final URI startSmsSetupEndpoint;
  private final URI completeSmsSetupEndpoint;
  private final URI startTotpSetupEndpoint;
  private final URI completeTotpSetupEndpoint;

  public MfaSetupFlows(MockSmsbox smsBox, URI baseUri, String issuer, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
    this.smsBox = smsBox;
    this.issuer = issuer;
    this.startSmsSetupEndpoint =
        UriBuilder.fromUri(baseUri).path(MfaSetupResource.PATH + "/sms/start").build();
    this.completeSmsSetupEndpoint =
        UriBuilder.fromUri(baseUri).path(MfaSetupResource.PATH + "/sms/complete").build();
    this.startTotpSetupEndpoint =
        UriBuilder.fromUri(baseUri).path(MfaSetupResource.PATH + "/totp/start").build();
    this.completeTotpSetupEndpoint =
        UriBuilder.fromUri(baseUri).path(MfaSetupResource.PATH + "/totp/complete").build();
  }

  public String getSecretFromQrCode(String base64qrImage, String email)
      throws IOException, NotFoundException {
    String pattern = "^otpauth:\\/\\/totp\\/" + issuer + ":" + email + "\\?secret=(.*)$";
    Pattern totpQrCodeSecretPattern = Pattern.compile(pattern);

    String text = QRCodeUtils.readQrImage(base64qrImage);
    Matcher matcher = totpQrCodeSecretPattern.matcher(text);
    if (!matcher.matches()) {
      throw new RuntimeException(
          "Failed to extract secret from text=" + text + " pattern=" + pattern);
    }
    return matcher.group(1);
  }

  public MfaTotpSetupStartDTO startTotpMfaSetupSuccess(String sessionId) {
    DataResponse<MfaTotpSetupStartDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .post(startTotpSetupEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    return dataResponse.getData();
  }

  public MfaChallengeCodeDetailsDTO startSmsMfaSetupSuccessFlow(String sessionId) {
    DataResponse<MfaChallengeCodeDetailsDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .post(startSmsSetupEndpoint)
            .then()
            .statusCode(200)
            .body(sameJson("{\"data\":{\"validitySeconds\":60}}"))
            .extract()
            .as(new TypeRef<>() {});

    return dataResponse.getData();
  }

  public MfaSetupDTO setupSmsSuccess(User user, String sessionId) throws IOException {
    startSmsMfaSetupSuccessFlow(sessionId);

    int code =
        AuthApiTestUtils.getLastSmsMessageVerificationCode(
            smsBox, user.getPhoneNumber().getNumber());

    DataResponse<MfaSetupDTO> dataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
            .post(completeSmsSetupEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(new TypeRef<>() {});

    return dataResponse.getData();
  }

  public Pair<MfaSetupDTO, String> setupTotpSuccess(User user, String sessionId)
      throws IOException, NotFoundException {
    String base64qrImage = startTotpMfaSetupSuccess(sessionId).getQrImage();
    String secret = getSecretFromQrCode(base64qrImage, user.getEmail());
    return Pair.of(completeTotpMfaSetupSuccessFlow(secret, sessionId), secret);
  }

  private MfaSetupDTO completeTotpMfaSetupSuccessFlow(String secret, String sessionId)
      throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeTotpSetupEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    AtomicReference<MfaSetupDTO> mfaSetup = new AtomicReference<>();
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              int code = TotpUtils.generateCurrentNumber(secret);

              DataResponse<MfaSetupDTO> dataResponse =
                  given()
                      .when()
                      .contentType(MediaType.APPLICATION_JSON)
                      .cookie(SsoSession.COOKIE_NAME, sessionId)
                      .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
                      .post(completeTotpSetupEndpoint)
                      .then()
                      .statusCode(200)
                      .extract()
                      .as(new TypeRef<>() {});

              mfaSetup.set(dataResponse.getData());
            });

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeTotpSetupEndpoint)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));

    return mfaSetup.get();
  }
}
