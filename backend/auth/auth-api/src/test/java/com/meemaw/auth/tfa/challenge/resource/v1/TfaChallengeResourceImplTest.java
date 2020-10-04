package com.meemaw.auth.tfa.challenge.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.session.resource.v1.SsoResource;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.auth.tfa.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.tfa.totp.impl.TotpUtils;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.utils.AuthApiSetupUtils;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
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
public class TfaChallengeResourceImplTest extends AbstractAuthApiTest {

  @Inject UserDatasource userDatasource;
  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;
  @Inject UserTfaDatasource userTfaDatasource;

  @Test
  public void get_tfa_challenge__should_throw__when_random_challenge_id() {
    given()
        .when()
        .get(TfaChallengeResource.PATH + "/random")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void get_tfa_challenge__should_throw_and_clear_cookie__when_missing_challenge_id() {
    given()
        .when()
        .get(TfaChallengeResource.PATH + "/" + UUID.randomUUID())
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"))
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_challenge__should_throw__when_invalid_content_type(String method) {
    String path = String.join("/", TfaChallengeResourceImpl.PATH, method, "complete");
    given()
        .when()
        .post(path)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_challenge__should_throw__when_no_body(String method) {
    String path = String.join("/", TfaChallengeResourceImpl.PATH, method, "complete");
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\",\"challengeId\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_challenge__should_throw__when_empty_body(String method) {
    String path = String.join("/", TfaChallengeResourceImpl.PATH, method, "complete");
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\",\"challengeId\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_challenge__should_throw__when_missing_verification_id_cookie(
      String method) throws JsonProcessingException {
    String path = String.join("/", TfaChallengeResourceImpl.PATH, method, "complete");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .post(path)
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
    String sessionId = authApi().signUpAndLogin(email, password);
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
            objectMapper.writeValueAsString(
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
  public void send_sms_challenge_code__should_throw__when_no_challenge_cookie() {
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
  public void send_sms_challenge_code__should_throw__when_random_challenge_cookie() {
    given()
        .cookie(SsoChallenge.COOKIE_NAME, "random")
        .when()
        .post(TfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }
}
