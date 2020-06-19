package com.meemaw.session.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(AuthApiTestResource.class)
public class SessionResourceValidationTest {

  @Test
  public void post_page_should_throw_error_when_unsupported_media_type() {
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
  public void post_page_should_throw_error_when_empty_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void post_page_should_throw_error_when_empty_json() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"doctype\":\"may not be null\",\"organizationId\":\"Required\",\"url\":\"may not be null\"}}}"));
  }

  @Test
  public void get_sessions_should_be_under_cookie_auth() {
    SsoTestSetupUtils.cookieExpect401(SessionResource.PATH, null);
    SsoTestSetupUtils.cookieExpect401(SessionResource.PATH, "random");
    SsoTestSetupUtils.cookieExpect401(SessionResource.PATH, SsoSession.newIdentifier());
  }

  @Test
  @Disabled
  public void get_page_should_be_under_cookie_auth() {
    String path =
        String.format(
            SessionResourceTest.SESSION_PAGE_PATH_TEMPLATE, UUID.randomUUID(), UUID.randomUUID());

    SsoTestSetupUtils.cookieExpect401(path, null);
    SsoTestSetupUtils.cookieExpect401(path, "random");
    SsoTestSetupUtils.cookieExpect401(path, SsoSession.newIdentifier());
  }
}
