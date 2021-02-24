package com.rebrowse.auth.utils;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeResponseDTO;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.mfa.challenge.resource.v1.AuthorizationMfaChallengeResource;
import com.rebrowse.auth.mfa.dto.MfaChallengeCodeDetailsDTO;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.mfa.totp.impl.TotpUtils;
import com.rebrowse.auth.mfa.totp.model.dto.MfaTotpSetupStartDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.shared.sms.MockSmsbox;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import io.restassured.common.mapper.TypeRef;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class MfaAuthorizationFlows extends AbstractTestFlow {

  private static final String basePath = AuthorizationMfaChallengeResource.PATH;

  private final URI startEnforcedTotpSetupEndpoint;
  private final URI completeEnforcedTotpChallengeEndpoint;
  private final URI completeTotpChallengeEndpoint;

  private final URI startEnforcedSmsSetupEndpoint;
  private final URI completeEnforcedSmsChallengeEndpoint;
  private final URI completeSmsChallengeEndpoint;
  private final URI sendSmsChallengeCodeEndpoint;

  private final MockSmsbox smsBox;

  public MfaAuthorizationFlows(URI baseUri, MockSmsbox smsBox, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
    this.smsBox = smsBox;

    this.startEnforcedSmsSetupEndpoint =
        UriBuilder.fromUri(baseUri).path(basePath + "/sms/setup").build();
    this.completeEnforcedSmsChallengeEndpoint =
        UriBuilder.fromUri(baseUri).path(basePath + "/sms/complete-enforced").build();
    this.completeSmsChallengeEndpoint = UriBuilder.fromUri(baseUri).path(basePath + "/sms").build();
    this.sendSmsChallengeCodeEndpoint =
        UriBuilder.fromUri(baseUri).path(basePath + "/sms/send_code").build();

    this.completeTotpChallengeEndpoint =
        UriBuilder.fromUri(baseUri).path(basePath + "/totp").build();
    this.completeEnforcedTotpChallengeEndpoint =
        UriBuilder.fromUri(baseUri).path(basePath + "/totp/complete-enforced").build();

    this.startEnforcedTotpSetupEndpoint =
        UriBuilder.fromUri(baseUri).path(basePath + "/totp/setup").build();
  }

  public MfaTotpSetupStartDTO startEnforcedSmsSetup(String challengeId) {
    DataResponse<MfaTotpSetupStartDTO> dataResponse =
        given()
            .when()
            .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
            .post(startEnforcedTotpSetupEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    return dataResponse.getData();
  }

  public MfaTotpSetupStartDTO startEnforcedTotpSetup(String challengeId) {
    DataResponse<MfaTotpSetupStartDTO> dataResponse =
        given()
            .when()
            .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
            .post(startEnforcedTotpSetupEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    return dataResponse.getData();
  }

  public MfaChallengeCodeDetailsDTO startEnforcedSmsChallengeSetup(String challengeId) {
    DataResponse<MfaChallengeCodeDetailsDTO> dataResponse =
        given()
            .when()
            .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
            .post(startEnforcedSmsSetupEndpoint)
            .then()
            .statusCode(200)
            .body(sameJson("{\"data\":{\"validitySeconds\":60}}"))
            .extract()
            .as(new TypeRef<>() {});

    return dataResponse.getData();
  }

  public MfaChallengeCodeDetailsDTO sendSmsChallengeCode(String challengeId) {
    DataResponse<MfaChallengeCodeDetailsDTO> dataResponse =
        given()
            .when()
            .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
            .get(sendSmsChallengeCodeEndpoint)
            .then()
            .statusCode(200)
            .body(sameJson("{\"data\":{\"validitySeconds\":60}}"))
            .extract()
            .as(new TypeRef<>() {});

    return dataResponse.getData();
  }

  public void completeSmsChallenge(String challengeId, String phoneNumber)
      throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeSmsChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    int code = AuthApiTestUtils.getLastSmsMessageVerificationCode(smsBox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(completeSmsChallengeEndpoint)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                objectMapper.writeValueAsString(
                    AuthApiTestData.LOCALHOST_AUTHORIZATION_SUCCESS_RESPONSE)))
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeSmsChallengeEndpoint)
        .then()
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");
  }

  public void completeEnforcedSmsChallenge(String challengeId, String phoneNumber)
      throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeEnforcedSmsChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    int code = AuthApiTestUtils.getLastSmsMessageVerificationCode(smsBox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(completeEnforcedSmsChallengeEndpoint)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                objectMapper.writeValueAsString(
                    AuthApiTestData.LOCALHOST_AUTHORIZATION_SUCCESS_RESPONSE)))
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeEnforcedSmsChallengeEndpoint)
        .then()
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");
  }

  public void completeEnforcedTotpChallenge(String secret, String challengeId)
      throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeEnforcedTotpChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              int code = TotpUtils.generateCurrentNumber(secret);
              given()
                  .when()
                  .contentType(MediaType.APPLICATION_JSON)
                  .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
                  .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
                  .post(completeEnforcedTotpChallengeEndpoint)
                  .then()
                  .statusCode(200)
                  .cookie(SsoSession.COOKIE_NAME)
                  .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "")
                  .body(
                      sameJson(
                          objectMapper.writeValueAsString(
                              AuthApiTestData.LOCALHOST_AUTHORIZATION_SUCCESS_RESPONSE)));
            });

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeEnforcedTotpChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");
  }

  public void completeTotpChallenge(String secret, String challengeId)
      throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeTotpChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              int code = TotpUtils.generateCurrentNumber(secret);
              given()
                  .when()
                  .contentType(MediaType.APPLICATION_JSON)
                  .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
                  .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
                  .post(completeTotpChallengeEndpoint)
                  .then()
                  .statusCode(200)
                  .cookie(SsoSession.COOKIE_NAME)
                  .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "")
                  .body(
                      sameJson(
                          objectMapper.writeValueAsString(
                              AuthApiTestData.LOCALHOST_AUTHORIZATION_SUCCESS_RESPONSE)));
            });

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123455)))
        .post(completeTotpChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");
  }

  public AuthorizationMfaChallengeResponseDTO completePwdChallengeToMfa(
      String email, String password, String passwordChallengeId) {
    DataResponse<AuthorizationMfaChallengeResponseDTO> dataResponse =
        new PwdAuthorizationFlows(baseUri, objectMapper)
            .completePwdChallenge(email, password, passwordChallengeId)
            .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME, "")
            .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME)
            .extract()
            .body()
            .as(new TypeRef<>() {});
    return dataResponse.getData();
  }
}
