package com.meemaw.session.sessions.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.cookieExpect401;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdmin;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.session.sessions.v1.SessionResource;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(AuthApiTestResource.class)
public class SessionResourceValidationTest {

  @Test
  public void post_page__should_throw_error__when_unsupported_media_type() {
    given()
        .when()
        .contentType(TEXT_PLAIN)
        .post(SessionResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"message\":\"Media type not supported.\",\"reason\":\"Unsupported Media Type\",\"statusCode\":415}}"));
  }

  @Test
  public void post_page__should_throw_error__when_empty_payload() {
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void post_page__should_throw__when_empty_json() {
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body("{}")
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"doctype\":\"may not be null\",\"organizationId\":\"Required\",\"url\":\"may not be null\"}}}"));
  }

  @Test
  public void post_page__should_throw_error__when_robot_user_agent()
      throws URISyntaxException, IOException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(payload)
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"You're a robot\"}}"));
  }

  @Test
  public void post_page__should_throw_error__when_hacker_user_agent()
      throws URISyntaxException, IOException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    given()
        .when()
        .contentType(APPLICATION_JSON)
        .header(HttpHeaders.USER_AGENT, "*".repeat(200))
        .body(payload)
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"You're a robot\"}}"));
  }

  @Test
  public void post_page__should_throw_error__when_empty_user_agent()
      throws URISyntaxException, IOException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    given()
        .when()
        .contentType(APPLICATION_JSON)
        .header(HttpHeaders.USER_AGENT, "")
        .body(payload)
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"userAgent\":\"Required\"}}}"));
  }

  @Test
  public void get_sessions__should_be_under_cookie_auth() {
    cookieExpect401(SessionResource.PATH, null);
    cookieExpect401(SessionResource.PATH, "random");
    cookieExpect401(SessionResource.PATH, SsoSession.newIdentifier());
  }

  @Test
  public void get_sessions__should_throw__on_unsupported_fields() {
    String path =
        SessionResource.PATH
            + "?random=gte:aba&aba=gtecaba&group_by=another&sort_by=hehe&limit=not_string";

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field in search query\",\"random\":\"Unexpected field in search query\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field in group_by query\"},\"sort_by\":{\"ehe\":\"Unexpected field in sort_by query\"}}}}"));
  }

  @Test
  @Disabled // TODO: enable once S2S auth
  public void get_page__should_be_under_cookie_auth() {
    String path =
        String.format(
            SessionResourceImplTest.SESSION_PAGE_PATH_TEMPLATE,
            UUID.randomUUID(),
            UUID.randomUUID());

    cookieExpect401(path, null);
    cookieExpect401(path, "random");
    cookieExpect401(path, SsoSession.newIdentifier());
  }
}
