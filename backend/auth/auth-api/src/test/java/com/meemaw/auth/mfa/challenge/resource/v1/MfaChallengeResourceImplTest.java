package com.meemaw.auth.mfa.challenge.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.mfa.MfaMethod;
import com.meemaw.auth.mfa.model.SsoChallenge;
import com.meemaw.auth.mfa.model.dto.ChallengeResponseDTO;
import com.meemaw.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.meemaw.auth.mfa.model.dto.MfaSetupDTO;
import com.meemaw.auth.mfa.setup.resource.v1.MfaSetupResource;
import com.meemaw.auth.mfa.sms.impl.MfaSmsProvider;
import com.meemaw.auth.mfa.totp.datasource.MfaTotpSetupDatasource;
import com.meemaw.auth.mfa.totp.impl.TotpUtils;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.session.resource.v1.SsoSessionResource;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.auth.user.phone.datasource.UserPhoneCodeDatasource;
import com.meemaw.auth.user.resource.v1.UserResource;
import com.meemaw.auth.utils.AuthApiSetupUtils;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.matchers.SameJSON;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.model.user.User;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import io.vertx.core.http.HttpHeaders;
import java.security.GeneralSecurityException;
import java.util.Collections;
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
public class MfaChallengeResourceImplTest extends AbstractAuthApiTest {

  @Inject MfaTotpSetupDatasource mfaTotpSetupDatasource;
  @Inject UserMfaDatasource userMfaDatasource;
  @Inject UserPhoneCodeDatasource userPhoneCodeDatasource;

  @Test
  public void get_tfa_challenge_user__should_throw__when_random_challenge_id() {
    given()
        .when()
        .get(MfaChallengeResource.PATH + "/random/user")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void get_tfa_challenge__should_throw__when_random_challenge_id() {
    given()
        .when()
        .get(MfaChallengeResource.PATH + "/random")
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
        .get(MfaChallengeResource.PATH + "/" + UUID.randomUUID())
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
    String path = String.join("/", MfaChallengeResourceImpl.PATH, method, "complete");
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
    String path = String.join("/", MfaChallengeResourceImpl.PATH, method, "complete");
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
    String path = String.join("/", MfaChallengeResourceImpl.PATH, method, "complete");
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
    String path = String.join("/", MfaChallengeResourceImpl.PATH, method, "complete");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(10)))
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
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    String sessionId = authApi().signUpAndLogin(email, password);
    User user = authApi().retrieveUserData(sessionId).getUser();

    String secret = AuthApiSetupUtils.setupTotpMfa(user.getId(), sessionId, mfaTotpSetupDatasource);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login");

    String challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    assertTrue(userMfaDatasource.delete(user.getId(), MfaMethod.TOTP).toCompletableFuture().join());

    // Complete when tfa is not set up should clean up verification id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(
            objectMapper.writeValueAsString(
                new MfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(secret))))
        .post(MfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TOTP MFA not configured\"}}"))
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  @Test
  public void send_sms_challenge_code__should_throw__when_no_challenge_cookie() {
    given()
        .when()
        .post(MfaChallengeResource.PATH + "/sms/send_code")
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
        .post(MfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void complete_sms_tfa__should_cleanup_code__when_success() throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    String sessionId =
        authApi().signUpAndLogin(email, password, new PhoneNumberDTO("+1", "223344"));

    User user = authApi().retrieveUserData(sessionId).getUser();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(String.format("%s/phone_number/verify/send_code", UserResource.PATH))
        .then()
        .statusCode(200)
        .body(SameJSON.sameJson("{\"data\":{\"validitySeconds\":60}}"));

    String verifyCodeKey = MfaSmsProvider.verifyCodeKey(sessionId, user.getId());
    int verifyCode =
        userPhoneCodeDatasource.getCode(verifyCodeKey).toCompletableFuture().join().get();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new MfaChallengeCompleteDTO(verifyCode)))
        .patch(String.format("%s/phone_number/verify", UserResource.PATH));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(MfaSetupResource.PATH + "/sms/start")
        .then()
        .statusCode(200);

    String setupCodeKey = MfaSmsProvider.setupCodeKey(sessionId, user.getId());
    int setupCode =
        userPhoneCodeDatasource.getCode(setupCodeKey).toCompletableFuture().join().get();

    DataResponse<MfaSetupDTO> dataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(setupCode)))
            .post(MfaSetupResource.PATH + "/sms/complete")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(new TypeRef<>() {});

    assertEquals(MfaMethod.SMS, dataResponse.getData().getMethod());

    // We are on login page!
    Response loginResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", user.getEmail())
            .param("password", password)
            .header(HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login")
            .then()
            .statusCode(200)
            .cookie(SsoChallenge.COOKIE_NAME)
            .extract()
            .response();

    String challengeId = loginResponse.getDetailedCookie(SsoChallenge.COOKIE_NAME).getValue();

    DataResponse<UserDTO> challengedUser =
        given()
            .when()
            .get(MfaChallengeResource.PATH + "/" + challengeId + "/user")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    assertEquals(challengedUser.getData().getId(), user.getId());

    DataResponse<ChallengeResponseDTO> loginDataResponse =
        loginResponse.body().as(new TypeRef<>() {});

    assertEquals(challengeId, loginDataResponse.getData().getChallengeId());
    assertEquals(
        Collections.singletonList(MfaMethod.SMS), loginDataResponse.getData().getMethods());

    // We are on challenge page now!
    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(MfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(200)
        .extract()
        .body()
        .as(new TypeRef<>() {});

    String challengeCodeKey = MfaSmsProvider.challengeCodeKey(challengeId);
    int challengeCode =
        userPhoneCodeDatasource.getCode(challengeCodeKey).toCompletableFuture().join().get();

    String newSessionId =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoChallenge.COOKIE_NAME, challengeId)
            .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(challengeCode)))
            .post(MfaChallengeResource.PATH + "/sms/complete")
            .then()
            .statusCode(204)
            .cookie(SsoChallenge.COOKIE_NAME, "")
            .extract()
            .detailedCookie(SsoSession.COOKIE_NAME)
            .getValue();

    assertEquals(authApi().retrieveUserData(sessionId), authApi().retrieveUserData(newSessionId));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(challengeCode)))
        .post(MfaChallengeResource.PATH + "/sms/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Challenge session expired\"}}"));
  }
}
