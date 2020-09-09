package com.meemaw.auth.sso.tfa;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static com.meemaw.test.setup.SsoTestSetupUtils.signUpAndLogin;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.NotFoundException;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.sso.tfa.challenge.model.SsoChallenge;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.challenge.resource.v1.TfaChallengeResource;
import com.meemaw.auth.sso.tfa.challenge.resource.v1.TfaChallengeResourceImpl;
import com.meemaw.auth.sso.tfa.setup.dto.TfaSetupDTO;
import com.meemaw.auth.sso.tfa.setup.resource.v1.TfaResource;
import com.meemaw.auth.sso.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.sso.tfa.totp.impl.TotpUtils;
import com.meemaw.auth.sso.tfa.totp.model.dto.TfaTotpSetupStartDTO;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.PhoneNumberDTO;
import com.meemaw.auth.utils.AuthApiSetupUtils;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.shared.sms.MockSmsbox;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class TfaChallengeResourceTest {

  @Inject UserDatasource userDatasource;
  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject UserTfaDatasource userTfaDatasource;
  @Inject MockSmsbox mockSmsbox;

  @Test
  public void get_challenge__should_throw__when_no_verification_cookie() {
    given()
        .when()
        .get(TfaChallengeResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"challengeId\":\"Required\"}}}"));
  }

  @Test
  public void get_challenge__should_throw_and_clear_cookie__when_missing_verification() {
    given()
        .when()
        .queryParam("id", "random")
        .get(TfaChallengeResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"))
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void get_setup_tfa__should_throw__when_not_authenticated(String method) {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .get(String.join("/", TfaResource.PATH, method, "setup"))
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void post_setup_tfa__should_throw__when_not_authenticated(String method) {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(String.join("/", TfaResource.PATH, method, "setup"))
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void post_setup_tfa__should_throw__when_invalid_content_type(String method) {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(String.join("/", TfaResource.PATH, method, "setup"))
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void post_setup_tfa__should_throw__when_no_body(String method) {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(String.join("/", TfaResource.PATH, method, "setup"))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void post_setup_tfa__should_throw__when_empty_body(String method) {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body("{}")
        .post(String.join("/", TfaResource.PATH, method, "setup"))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));
  }

  @Test
  public void setup_top_tfa_complete__should_throw__when_missing_qr_request()
      throws JsonProcessingException {
    String sessionId =
        signUpAndLogin(
            mailbox,
            objectMapper,
            "setup-tfa-missing-qa-request@gmail.com",
            "setup-tfa-missing-qa-request");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .post(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Code expired\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa__should_throw__when_invalid_content_type(String method) {
    given()
        .when()
        .post(String.join("/", TfaChallengeResourceImpl.PATH, method, "complete"))
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa__should_throw__when_no_body(String method) {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(String.join("/", TfaChallengeResourceImpl.PATH, method, "complete"))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\",\"challengeId\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa__should_throw__when_empty_body(String method) {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(String.join("/", TfaChallengeResourceImpl.PATH, method, "complete"))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\",\"challengeId\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa__should_throw__when_missing_verification_id_cookie(String method)
      throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .post(String.join("/", TfaChallengeResourceImpl.PATH, method, "complete"))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"challengeId\":\"Required\"}}}"));
  }

  @Test
  public void complete_totp_tfa__should_throw_and_clear_cookie__when_tfa_not_configured()
      throws JsonProcessingException, GeneralSecurityException {
    String email = "tfa-complete-not-configured-flow@gmail.com";
    String password = "tfa-complete-not-configured-flow";
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);
    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    String secret = AuthApiSetupUtils.setupTotpTfa(userId, sessionId, tfaTotpSetupDatasource);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000")
            .post(SsoResource.PATH + "/login");

    String challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    assertTrue(userTfaDatasource.delete(userId, TfaMethod.TOTP).toCompletableFuture().join());

    // Complete when tfa is not set up should clean up verification id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(
            JacksonMapper.get()
                .writeValueAsString(
                    new TfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(secret))))
        .post(TfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TOTP TFA not configured\"}}"))
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  @Test
  public void setup_tfa__should_work__on_full_totp_flow()
      throws IOException, GeneralSecurityException, NotFoundException {
    String password = UUID.randomUUID().toString();
    String email = password + "@gmail.com";
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);

    DataResponse<TfaTotpSetupStartDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(TfaResource.PATH + "/totp/setup")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    String secret = tfaTotpSetupDatasource.getTotpSecret(userId).toCompletableFuture().join().get();
    assertEquals(
        String.format("otpauth://totp/Insight:%s?secret=%s", email, secret),
        TotpUtils.readBarcode(dataResponse.getData().getQrImage()).getText());

    // Complete tfa setup fails on invalid code
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(15)))
        .post(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    int tfaCode = TotpUtils.generateCurrentNumber(secret);

    // Complete tfa setup works on valid code
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
            .post(TfaResource.PATH + "/totp/setup");

    DataResponse<TfaSetupDTO> responseData = response.as(new TypeRef<>() {});
    response
        .then()
        .statusCode(200)
        .body(sameJson(JacksonMapper.get().writeValueAsString(responseData)));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .post(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Code expired\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TOTP TFA already set up\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000")
            .post(SsoResource.PATH + "/login");

    String challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    response
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\": {\"methods\": [\"totp\"], \"challengeId\": \"%s\"}}", challengeId)))
        .cookie(SsoChallenge.COOKIE_NAME, challengeId);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .post(TfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoChallenge.COOKIE_NAME, challengeId)
            .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
            .post(TfaChallengeResourceImpl.PATH + "/totp/complete");

    response
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");

    // new session id from TFA flow
    sessionId = response.detailedCookie(SsoSession.COOKIE_NAME).getValue();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TOTP TFA already set up\"}}"));

    // should clean up verification id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
        .post(TfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TFA challenge session expired\"}}"))
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  @Test
  public void sms_send_code__should_throw__when_no_challenge_cookie() {
    given()
        .when()
        .post(TfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"challengeId\":\"Required\"}}}"));
  }

  @Test
  public void setup_tfa__should_work__on_full_sms_flow() throws JsonProcessingException {
    String email = "tfa-sms-full-flow@gmail.com";
    String password = "tfa-sms-full-flow";
    PhoneNumberDTO phoneNumber = new PhoneNumberDTO("+386", "51222333");
    String sessionId =
        SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password, phoneNumber);

    AuthApiSetupUtils.setupSmsTfa(phoneNumber, sessionId, mockSmsbox);
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000")
            .post(SsoResource.PATH + "/login");

    String challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    response
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\": {\"methods\": [\"sms\"], \"challengeId\": \"%s\"}}", challengeId)))
        .cookie(SsoChallenge.COOKIE_NAME, challengeId);

    // 404 before any code is sent
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(123)))
        .post(TfaChallengeResource.PATH + "/sms/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(TfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"validitySeconds\":60}}"));

    int code = AuthApiSetupUtils.getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(code)))
        .post(TfaChallengeResourceImpl.PATH + "/sms/complete")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");

    // Should cleanup challenge id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(code)))
        .post(TfaChallengeResourceImpl.PATH + "/sms/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TFA challenge session expired\"}}"));
  }

  @Test
  public void setup_tfa__should_work__on_full_sms_and_totp_flow()
      throws JsonProcessingException, GeneralSecurityException {
    String email = "tfa-sms-and-totp-full-flow@gmail.com";
    String password = "tfa-sms-and-top-full-flow";
    PhoneNumberDTO phoneNumber = new PhoneNumberDTO("+386", "51222334");
    String sessionId =
        SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password, phoneNumber);

    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    AuthApiSetupUtils.setupSmsTfa(phoneNumber, sessionId, mockSmsbox);
    String tfaSecret = AuthApiSetupUtils.setupTotpTfa(userId, sessionId, tfaTotpSetupDatasource);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaResource.PATH)
        .then()
        .statusCode(200)
        .body("data.size()", is(2));

    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000")
            .post(SsoResource.PATH + "/login");

    String challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    response
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\": {\"methods\": [\"sms\", \"totp\"], \"challengeId\": \"%s\"}}",
                    challengeId)))
        .cookie(SsoChallenge.COOKIE_NAME, challengeId);

    given()
        .when()
        .queryParam("id", challengeId)
        .get(TfaChallengeResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"sms\",\"totp\"]}"));

    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(TfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"validitySeconds\":60}}"));

    // sms
    int code = AuthApiSetupUtils.getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(code)))
        .post(TfaChallengeResourceImpl.PATH + "/sms/complete")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");

    // totp (needs new challenge)
    response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000")
            .post(SsoResource.PATH + "/login");

    challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(
            JacksonMapper.get()
                .writeValueAsString(
                    new TfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(tfaSecret))))
        .post(TfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }
}
