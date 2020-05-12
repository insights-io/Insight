package com.meemaw.session.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(AuthApiTestResource.class)
public class SessionResourceValidationTest {

  @Test
  public void postPage_shouldThrowError_whenUnsupportedMediaType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(SessionResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"message\":\"Media type not supported.\",\"reason\":\"Unsupported Media Type\",\"statusCode\":415}}"));
  }

  @Test
  public void postPage_shouldThrowError_whenEmptyPayload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg0\":\"Payload is required\"}}}"));
  }

  @Test
  public void postPage_shouldThrowError_whenEmptyJson() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"doctype\":\"may not be null\",\"orgId\":\"Organization ID is required\",\"url\":\"may not be null\"}}}"));
  }

  @Test
  public void countPages_shouldThrowError_whenUnauthenticated() {
    given()
        .when()
        .get(SessionResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void countPages_shouldThrowError_whenRandomSessionId() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .get(SessionResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void countPages_shouldThrowError_whenInvalidSessionId() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .get(SessionResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void getPage_shouldThrowError_whenInvalidOrgLength() {
    String path =
        String.format("%s/%s/pages/%s", SessionResource.PATH, UUID.randomUUID(), UUID.randomUUID());
    given()
        .when()
        .queryParam("orgID", "qeweqewq")
        .get(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg2\":\"Organization ID must be 6 characters long\"}}}"));
  }
}
