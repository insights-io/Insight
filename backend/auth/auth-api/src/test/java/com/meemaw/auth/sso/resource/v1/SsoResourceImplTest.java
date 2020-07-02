package com.meemaw.auth.sso.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.signUpAndLogin;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.resource.v1.SignUpResource;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SsoResourceImplTest {

  @Inject MockMailbox mailbox;
  @Inject UserDatasource userDatasource;
  @Inject ObjectMapper objectMapper;

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  @Test
  public void login_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void login_should_fail_when_no_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void login_should_fail_when_invalid_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "random")
        .param("password", "random")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\",\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void login_should_fail_when_invalid_credentials() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "test@gmail.com")
        .param("password", "superFancyPassword")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }

  @Test
  public void login_should_fail_when_user_with_unfinished_signUp() throws JsonProcessingException {
    SignUpRequestDTO signUpRequestDTO =
        SsoTestSetupUtils.signUpRequestMock("login-no-complete@gmail.com", "password123");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signUpRequestDTO.getEmail())
        .param("password", "superFancyPassword")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }

  @Test
  public void logout_should_fail_when_no_cookie() {
    given()
        .when()
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void logout_should_fail_and_clear_cookie_on_random_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"))
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout_should_clear_cookie_on_existing_cookie() throws JsonProcessingException {
    String sessionId =
        signUpAndLogin(mailbox, objectMapper, "test-logout@gmail.com", "test-logout-password");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout_from_all_devices_should_fail_when_no_cookie() {
    given()
        .when()
        .post(SsoResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void logout_from_all_devices_should_fail_when_random_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(SsoResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout_from_all_devices_should_work_on_existing_session()
      throws JsonProcessingException {
    String email = "test-logout-from-all-devices@gmail.com";
    String password = "test-logout-password";

    String firstSessionId = signUpAndLogin(mailbox, objectMapper, email, password);
    String secondSessionId = SsoTestSetupUtils.login(email, password, null);

    // Make sure sessions are not the same
    assertNotEquals(firstSessionId, secondSessionId);

    DataResponse<UserDTO> firstSessionIdUser =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, firstSessionId)
            .get(SsoResource.PATH + "/me")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    DataResponse<UserDTO> secondSessionIdUser =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, secondSessionId)
            .get(SsoResource.PATH + "/me")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    // Make sure both sessions are associated with same user
    assertEquals(firstSessionIdUser, secondSessionIdUser);
    assertEquals(email, firstSessionIdUser.getData().getEmail());

    // Logout from all sessions
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .post(SsoResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    // first session is deleted
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    // second session id is deleted
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void session_should_fail_when_no_sessionId() {
    given()
        .when()
        .get(SsoResource.PATH + "/session")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void session_should_clear_session_cookie_when_missing_sessionId() {
    given()
        .when()
        .queryParam("id", "random")
        .get(SsoResource.PATH + "/session")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void me_should_fail_when_missing_sessionId_cookie() {
    given()
        .when()
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void me_should_clear_session_cookie_when_missing_sessionId() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .queryParam("id", "random")
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void sso_flow_should_work_with_registered_user() throws JsonProcessingException {
    String email = "sso_flow_test@gmail.com";
    String password = "sso_flow_test_password";
    String sessionId = signUpAndLogin(mailbox, objectMapper, email, password);

    AuthUser authUser = userDatasource.findUser(email).toCompletableFuture().join().orElseThrow();

    // should be able to get session by id
    given()
        .when()
        .queryParam("id", sessionId)
        .get(SsoResource.PATH + "/session")
        .then()
        .statusCode(200)
        .body(sameJson(objectMapper.writeValueAsString(DataResponse.data(authUser))));

    // should be able to get session via cookie
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(200)
        .body(sameJson(objectMapper.writeValueAsString(DataResponse.data(authUser))));

    // should be able to logout
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }
}
