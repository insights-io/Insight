package com.rebrowse.auth.sso.session.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.api.query.QueryParam;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.model.auth.UserData;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.model.user.User;
import com.rebrowse.model.user.UserSearchParams;
import com.rebrowse.shared.rest.response.DataResponse;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class SsoSessionResourceImplTest extends AbstractAuthApiQuarkusTest {

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  @Test
  public void logout__should_fail__when_no_cookie() {
    given()
        .when()
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void logout__should_fail_and_clear_cookie__when_random_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"))
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout__should_clear_cookie__when_existing_cookie() throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout__should_clear_session_from_future_sessions_lookups()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();
    String firstSessionId = signUpFlows().signUpAndLogin(email, password);

    String secondSessionId = authorizationFlows().login(email, password);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                objectMapper.writeValueAsString(
                    DataResponse.data(List.of(firstSessionId, secondSessionId)))));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(objectMapper.writeValueAsString(DataResponse.data(List.of(firstSessionId)))));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void logout_from_all_devices__should_fail__when_no_cookie() {
    given()
        .when()
        .post(SsoSessionResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void logout_from_all_devices__should_fail__when_random_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(SsoSessionResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout_from_all_devices__should_work__when_existing_session()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();

    String firstSessionId = signUpFlows().signUpAndLogin(email, password);
    String secondSessionId = authorizationFlows().login(email, password);

    // Make sure sessions are not the same
    assertNotEquals(firstSessionId, secondSessionId);

    User firstUser =
        UserData.retrieve(sdkRequest().sessionId(firstSessionId).build())
            .toCompletableFuture()
            .join()
            .getUser();

    User secondUser =
        UserData.retrieve(sdkRequest().sessionId(secondSessionId).build())
            .toCompletableFuture()
            .join()
            .getUser();

    // Make sure both sessions are associated with same user
    assertEquals(firstUser.getId(), secondUser.getId());
    assertEquals(email, firstUser.getEmail());

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                objectMapper.writeValueAsString(
                    DataResponse.data(List.of(firstSessionId, secondSessionId)))));

    // Logout from all sessions
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .post(SsoSessionResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    // first session is deleted
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoSessionResource.PATH + "/session/userdata")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    // second session id is deleted
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoSessionResource.PATH + "/session/userdata")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void session_should_fail_when_no_sessionId() {
    given()
        .when()
        .get(SsoSessionResource.PATH + "/session/userdata")
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
        .get(SsoSessionResource.PATH + "/session/random/userdata")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void me_should_fail_when_missing_sessionId_cookie() {
    given()
        .when()
        .get(SsoSessionResource.PATH + "/session/userdata")
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
        .get(SsoSessionResource.PATH + "/session/userdata")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void sso_flow_should_work_with_registered_user() throws JsonProcessingException {
    String email = "sso_flow_test@gmail.com";
    String password = "sso_flow_test_password";
    String sessionId = signUpFlows().signUpAndLogin(email, password);

    User user =
        Organization.members(
                UserSearchParams.builder().email(QueryParam.eq(email)).build(),
                sdkRequest().sessionId(sessionId).build())
            .toCompletableFuture()
            .join()
            .get(0);

    Organization organization =
        Organization.retrieve(sdkRequest().sessionId(sessionId).build())
            .toCompletableFuture()
            .join();

    // should be able to get session by id
    UserData userData =
        UserData.retrieve(sessionId, sdkRequest().build()).toCompletableFuture().join();
    assertEquals(userData, new UserData(user, organization));

    // should be able to get session via cookie
    userData =
        UserData.retrieve(sdkRequest().sessionId(sessionId).build()).toCompletableFuture().join();

    assertEquals(userData, new UserData(user, organization));

    // should be able to logout
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void sessions__should_fail__when_missing_session_id_cookie() {
    given()
        .when()
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void sessions_should_fail_when_random_sessionId_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void sessions__should_return_collection__when_existing_sessionId_cookie()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(sameJson(objectMapper.writeValueAsString(DataResponse.data(List.of(sessionId)))));
  }
}
