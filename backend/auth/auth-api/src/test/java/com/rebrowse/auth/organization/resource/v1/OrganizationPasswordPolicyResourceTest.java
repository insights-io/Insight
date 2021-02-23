package com.rebrowse.auth.organization.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.auth.organization.model.dto.PasswordPolicyDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.test.utils.RestAssuredUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class OrganizationPasswordPolicyResourceTest extends AbstractAuthApiQuarkusTest {

  @Test
  public void create__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.POST, OrganizationPasswordPolicyResource.PATH, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.POST, OrganizationPasswordPolicyResource.PATH, ContentType.JSON);
  }

  @Test
  public void create__should_throw__when_no_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .post(OrganizationPasswordPolicyResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                " {\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void create__should_throw__when_empty_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(OrganizationPasswordPolicyResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                " {\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void create__should_throw__when_constraint_violations() throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(Map.of("minCharacters", 2)))
        .post(OrganizationPasswordPolicyResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"minCharacters\":\"Password should be at least 8 characters long\"}}}"));
  }

  @Test
  public void create__should_create_and_update_but_throw__when_duplicate()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    DataResponse<PasswordPolicyDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(objectMapper.writeValueAsString(Map.of("minCharacters", 10)))
            .post(OrganizationPasswordPolicyResource.PATH)
            .then()
            .statusCode(201)
            .extract()
            .as(new TypeRef<>() {});

    assertEquals(10, dataResponse.getData().getMinCharacters());
    assertTrue(dataResponse.getData().isPreventPasswordReuse());
    assertFalse(dataResponse.getData().isRequireLowercaseCharacter());

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(Map.of("minCharacters", 10)))
        .post(OrganizationPasswordPolicyResource.PATH)
        .then()
        .statusCode(409)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":409,\"reason\":\"Conflict\",\"message\":\"Conflict\"}}"));

    dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                objectMapper.writeValueAsString(
                    Map.of("minCharacters", 12, "requireLowercaseCharacter", true)))
            .patch(OrganizationPasswordPolicyResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    assertEquals(12, dataResponse.getData().getMinCharacters());
    assertTrue(dataResponse.getData().isPreventPasswordReuse());
    assertTrue(dataResponse.getData().isRequireLowercaseCharacter());

    DataResponse<PasswordPolicyDTO> getDataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(OrganizationPasswordPolicyResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    assertEquals(dataResponse, getDataResponse);
  }

  @Test
  public void update__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.PATCH, OrganizationPasswordPolicyResource.PATH, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.PATCH, OrganizationPasswordPolicyResource.PATH, ContentType.JSON);
  }

  @Test
  public void update__should_throw__when_no_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .patch(OrganizationPasswordPolicyResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                " {\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void update__should_throw__when_empty_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .patch(OrganizationPasswordPolicyResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                " {\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void update__should_throw__when_constraint_violations() throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(Map.of("minCharacters", 2)))
        .patch(OrganizationPasswordPolicyResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"minCharacters\":\"Password should be at least 8 characters long\"}}}"));
  }

  @Test
  public void get__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, OrganizationPasswordPolicyResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, OrganizationPasswordPolicyResource.PATH);
  }

  @Test
  public void get__should_throw__when_not_found() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(OrganizationPasswordPolicyResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }
}
