package com.meemaw.auth.user.resource.v1;

import static com.meemaw.shared.SharedConstants.INSIGHT_ORGANIZATION_ID;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.AuthApiTestProvider.INSIGHT_ADMIN_EMAIL;
import static com.meemaw.test.setup.AuthApiTestProvider.INSIGHT_ADMIN_ID;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.session.resource.v1.SsoResource;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.model.dto.SessionInfoDTO;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.auth.utils.AuthApiSetupUtils;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.shared.sms.MockSmsbox;
import com.meemaw.test.matchers.SameJSON;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class UserResourceImplTest extends AbstractAuthApiTest {

  private static final String USER_ID_PATH =
      UserResource.PATH + "/4122c933-c30c-4948-9009-3f7ab6501bd9";

  private static final String PHONE_NUMBER_VERIFY_PATH =
      String.join("/", UserResource.PATH, "phone_number", "verify");

  private static final String PHONE_NUMBER_VERIFY_SEND_CODE_PATH =
      String.join("/", PHONE_NUMBER_VERIFY_PATH, "send_code");

  @Inject MockSmsbox mockSmsbox;

  @Test
  public void phone_number_verify_send_code__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, PHONE_NUMBER_VERIFY_SEND_CODE_PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, PHONE_NUMBER_VERIFY_SEND_CODE_PATH);
  }

  @Test
  public void phone_number_verify_send_code__should_throw__when_no_phone_number()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(PHONE_NUMBER_VERIFY_SEND_CODE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .post(PHONE_NUMBER_VERIFY_SEND_CODE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_invalid_content_type() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.PATCH, PHONE_NUMBER_VERIFY_PATH, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.PATCH, PHONE_NUMBER_VERIFY_PATH, ContentType.JSON);
  }

  @Test
  public void phone_number_verify__should_throw__when_no_body() {
    String sessionId = authApi().loginWithInsightAdmin();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_empty_body() {
    String sessionId = authApi().loginWithInsightAdmin();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body("{}")
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_invalid_code()
      throws JsonProcessingException {
    PhoneNumberDTO phoneNumber = new PhoneNumberDTO("+386", "512121");
    String sessionId = authApi().signUpAndLoginWithRandomCredentials(phoneNumber);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_no_phone_number()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {UserResource.PATH, USER_ID_PATH})
  public void update_user__should_throw__when_invalid_content_type(String path) {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .patch(path)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {UserResource.PATH, USER_ID_PATH})
  public void update_user__should_throw__when_unauthorized(String path) {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.PATCH, path, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.PATCH, path, ContentType.JSON);
  }

  @ParameterizedTest
  @ValueSource(strings = {UserResource.PATH, USER_ID_PATH})
  public void update_user__should_throw__when_no_body(String path) {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .patch(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .patch(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {UserResource.PATH, USER_ID_PATH})
  public void update_user__should_throw__when_empty_body(String path) {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .patch(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body("{}")
        .patch(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {UserResource.PATH, USER_ID_PATH})
  public void update_user__should_throw__when_invalid_body(String path)
      throws JsonProcessingException {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(Map.of("a", "b")))
        .patch(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"a\":\"Unexpected field\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(JacksonMapper.get().writeValueAsString(Map.of("a", "b")))
        .patch(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"a\":\"Unexpected field\"}}}"));
  }

  @Test
  public void update_user__should_work__when_valid_body() throws JsonProcessingException {
    String sessionId = authApi().loginWithInsightAdmin();

    PhoneNumber updatedPhoneNumber = new PhoneNumberDTO("+386", "51222333");
    DataResponse<UserDTO> updateUserDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(
                JacksonMapper.get().writeValueAsString(Map.of("phone_number", updatedPhoneNumber)))
            .patch(UserResource.PATH)
            .as(new TypeRef<>() {});

    assertEquals(updatedPhoneNumber, updateUserDataResponse.getData().getPhoneNumber());
    assertFalse(updateUserDataResponse.getData().isPhoneNumberVerified());

    // Should also update the sessions
    DataResponse<SessionInfoDTO> getSessionInfoDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(String.join("/", SsoResource.PATH, "me"))
            .as(new TypeRef<>() {});

    assertEquals(updateUserDataResponse.getData(), getSessionInfoDataResponse.getData().getUser());

    // Send code for phone number verification
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(PHONE_NUMBER_VERIFY_SEND_CODE_PATH)
        .then()
        .statusCode(200)
        .body(SameJSON.sameJson("{\"data\":{\"validitySeconds\":60}}"));

    int verificationCode =
        AuthApiSetupUtils.getLastSmsMessageVerificationCode(
            mockSmsbox, updateUserDataResponse.getData().getPhoneNumber());

    // Verify phone number
    updateUserDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(
                JacksonMapper.get()
                    .writeValueAsString(new TfaChallengeCompleteDTO(verificationCode)))
            .patch(PHONE_NUMBER_VERIFY_PATH)
            .as(new TypeRef<>() {});

    assertTrue(updateUserDataResponse.getData().isPhoneNumberVerified());

    // Should also update the sessions
    getSessionInfoDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(String.join("/", SsoResource.PATH, "me"))
            .as(new TypeRef<>() {});
    assertEquals(updateUserDataResponse.getData(), getSessionInfoDataResponse.getData().getUser());
  }

  @Test
  public void me__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, UserResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, UserResource.PATH);
  }

  @Test
  public void me__should_return_current_user__when_authorized() {
    String sessionId = authApi().loginWithInsightAdmin();
    DataResponse<UserDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(UserResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    assertEquals(INSIGHT_ADMIN_ID, dataResponse.getData().getId());
    assertEquals(INSIGHT_ADMIN_EMAIL, dataResponse.getData().getEmail());
    assertEquals(INSIGHT_ORGANIZATION_ID, dataResponse.getData().getOrganizationId());

    String authToken = authApi().createAuthToken(sessionId);
    dataResponse =
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
            .get(UserResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    assertEquals(INSIGHT_ADMIN_ID, dataResponse.getData().getId());
    assertEquals(INSIGHT_ADMIN_EMAIL, dataResponse.getData().getEmail());
    assertEquals(INSIGHT_ORGANIZATION_ID, dataResponse.getData().getOrganizationId());
  }

  @Test
  public void get_user__should_throw__when_unauthorized() {
    String getUserPath = UserResource.PATH + "/" + UUID.randomUUID().toString();
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, getUserPath);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, getUserPath);
  }

  @Test
  public void get_user__should_throw__when_unauthorized_to_access_different_user()
      throws JsonProcessingException {
    String getDifferentUserPath = UserResource.PATH + "/" + INSIGHT_ADMIN_ID;

    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(getDifferentUserPath)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .get(getDifferentUserPath)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }
}
