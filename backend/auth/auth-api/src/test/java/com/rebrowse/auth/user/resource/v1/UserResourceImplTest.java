package com.rebrowse.auth.user.resource.v1;

import static com.rebrowse.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;
import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.sso.session.resource.v1.SsoSessionResource;
import com.rebrowse.auth.user.datasource.UserTable;
import com.rebrowse.auth.user.model.PhoneNumber;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.auth.user.model.dto.UserDTO;
import com.rebrowse.auth.user.model.dto.UserDataDTO;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.model.user.PhoneNumberUpdateParams;
import com.rebrowse.model.user.User;
import com.rebrowse.net.RequestOptions;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.shared.sms.MockSmsbox;
import com.rebrowse.test.matchers.SameJSON;
import com.rebrowse.test.rest.mappers.JacksonMapper;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class UserResourceImplTest extends AbstractAuthApiQuarkusTest {

  private static final String USER_ID_PATH =
      UserResource.PATH + "/3f70969a-fb58-44a3-8e34-2316a145cad3";

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
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentialsNoPhoneNumber();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(PHONE_NUMBER_VERIFY_SEND_CODE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
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

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_empty_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
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

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .body("{}")
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_no_send_code_session()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();

    // 404 before send_code session
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new MfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));

    // 404 before send_code session
    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .body(JacksonMapper.get().writeValueAsString(new MfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_no_phone_number()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentialsNoPhoneNumber();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new MfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .body(JacksonMapper.get().writeValueAsString(new MfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));
  }

  @Test
  public void update_user_phone_number__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.PATCH, UserResource.PATH + "/phone_number", ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.PATCH, UserResource.PATH + "/phone_number", ContentType.JSON);
  }

  @Test
  public void update_user_phone_number__should_throw__when_invalid_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .patch(UserResource.PATH + "/phone_number")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"countryCode\":\"Required\",\"digits\":\"Required\"}}}"));
  }

  @Test
  public void update_user_phone_number__should_work__when_valid_body()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    RequestOptions requestOptions = sdkRequest().sessionId(sessionId).build();

    User afterFirstUpdate =
        User.updatePhoneNumber(
                PhoneNumberUpdateParams.builder().countryCode("+386").digits("51123456").build(),
                requestOptions)
            .toCompletableFuture()
            .join();

    assertEquals("+38651123456", afterFirstUpdate.getPhoneNumber().getNumber());

    Map<String, ?> update = new LinkedHashMap<>(1);
    update.put(UserTable.PHONE_NUMBER, null);

    DataResponse<User> dataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(clientObjectMapper.writeValueAsString(update))
            .patch(UserResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    assertNull(dataResponse.getData().getPhoneNumber());
    assertEquals(afterFirstUpdate.getFullName(), dataResponse.getData().getFullName());
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
    String sessionId = authorizationFlows().loginWithAdminUser();
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

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
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

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
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

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    String sessionId = authorizationFlows().loginWithAdminUser();

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
    DataResponse<UserDataDTO> getSessionInfoDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(String.join("/", SsoSessionResource.PATH, "session", "userdata"))
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
        AuthApiTestUtils.getLastSmsMessageVerificationCode(
            mockSmsbox, updateUserDataResponse.getData().getPhoneNumber());

    // Verify phone number
    updateUserDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(
                JacksonMapper.get()
                    .writeValueAsString(new MfaChallengeCompleteDTO(verificationCode)))
            .patch(PHONE_NUMBER_VERIFY_PATH)
            .as(new TypeRef<>() {});

    assertTrue(updateUserDataResponse.getData().isPhoneNumberVerified());

    // Should also update the sessions
    getSessionInfoDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(String.join("/", SsoSessionResource.PATH, "session", "userdata"))
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    DataResponse<UserDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(UserResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    assertEquals(GlobalTestData.REBROWSE_ADMIN_ID, dataResponse.getData().getId());
    assertEquals(GlobalTestData.REBROWSE_ADMIN_EMAIL, dataResponse.getData().getEmail());
    assertEquals(REBROWSE_ORGANIZATION_ID, dataResponse.getData().getOrganizationId());

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    dataResponse =
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .get(UserResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    assertEquals(GlobalTestData.REBROWSE_ADMIN_ID, dataResponse.getData().getId());
    assertEquals(GlobalTestData.REBROWSE_ADMIN_EMAIL, dataResponse.getData().getEmail());
    assertEquals(REBROWSE_ORGANIZATION_ID, dataResponse.getData().getOrganizationId());
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
    String getDifferentUserPath = UserResource.PATH + "/" + GlobalTestData.REBROWSE_ADMIN_ID;

    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(getDifferentUserPath)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .get(getDifferentUserPath)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }
}
