package com.rebrowse.auth.mfa.setup.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.NotFoundException;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.user.resource.v1.UserResource;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.model.user.PhoneNumber;
import com.rebrowse.model.user.User;
import com.rebrowse.shared.sms.MockSmsbox;
import com.rebrowse.test.utils.RestAssuredUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import java.io.IOException;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@Tag("integration")
public class MfaSetupResourceImplTest extends AbstractAuthApiQuarkusTest {

  @Inject MockSmsbox mockSmsbox;

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void start_tfa_setup__should_throw__when_unauthorized(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method, "start");
    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, path);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_not_authenticated(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method, "complete");
    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, path, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, path, ContentType.JSON);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_invalid_content_type(String method) {
    String sessionId = authorizationFlows().loginWithAdminUser();
    String path = String.join("/", MfaSetupResource.PATH, method, "complete");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(path)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_no_body(String method) {
    String sessionId = authorizationFlows().loginWithAdminUser();
    String path = String.join("/", MfaSetupResource.PATH, method, "complete");
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_empty_body(String method) {
    String sessionId = authorizationFlows().loginWithAdminUser();
    String path = String.join("/", MfaSetupResource.PATH, method, "complete");
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .post(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));
  }

  @Test
  public void complete_totp_tfa_setup__should_throw__when_missing_qr_request()
      throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();

    // 404 before any code is sent
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(10)))
        .post(MfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));
  }

  @Test
  public void list_tfa_setups__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, MfaSetupResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, MfaSetupResource.PATH);
  }

  @Test
  public void list_tfa_setups__should_return_empty_list__when_no_tfa_configured() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(MfaSetupResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .get(MfaSetupResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }

  @Test
  public void get_tfa_setup__should_throw__when_random_method() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(MfaSetupResource.PATH + "/random")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void get_tfa_setup__should_throw__when_tfa_no_configured(String method) {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(String.join("/", MfaSetupResource.PATH, method))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_tfa_setup__should_throw__when_unauthorized(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method);
    RestAssuredUtils.ssoSessionCookieTestCases(Method.DELETE, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.DELETE, path);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_tfa__should_throw__when_user_without_tfa(String method) {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(String.join("/", MfaSetupResource.PATH, method))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void delete_totp_tfa__should_succeed__when_user_with_tfa()
      throws IOException, NotFoundException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    User user = authorizationFlows().retrieveUserData(sessionId).getUser();
    mfaSetupFlows().setupTotpSuccess(user, sessionId);

    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(MfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(200);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(MfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(204);

    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(MfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void complete_sms_mfa_setup__should_throw__when_invalid_auth_identifier()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    String sessionId = signUpFlows().signUpAndLogin(email, password);
    PhoneNumber phoneNumber =
        authorizationFlows().retrieveUserData(sessionId).getUser().getPhoneNumber();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(UserResource.PATH + "/phone_number/verify/send_code")
        .then()
        .statusCode(200);

    int phoneNumberVerificationCode =
        AuthApiTestUtils.getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber.getNumber());
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            objectMapper.writeValueAsString(
                new MfaChallengeCompleteDTO(phoneNumberVerificationCode)))
        .patch(UserResource.PATH + "/phone_number/verify")
        .then()
        .statusCode(200);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(MfaSetupResource.PATH + "/sms/start")
        .then()
        .statusCode(200);

    String secondSessionId = authorizationFlows().login(email, password);
    int code =
        AuthApiTestUtils.getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber.getNumber());
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(MfaSetupResource.PATH + "/sms/complete")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));
  }
}
