package com.meemaw.session.resource.v1;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
public class SessionResourceTest {

  public static final String SESSION_PATH_TEMPLATE = String.join("/", SessionResource.PATH, "%s");

  public static final String SESSION_PAGE_PATH_TEMPLATE =
      String.join("/", SESSION_PATH_TEMPLATE, "pages", "%s");

  @Inject ObjectMapper objectMapper;

  @Test
  public void post_page_should_link_sessions_when_matching_device_id()
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    DataResponse<PageIdentity> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body(payload)
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    PageIdentity pageIdentity = dataResponse.getData();
    UUID deviceId = pageIdentity.getDeviceId();
    UUID sessionId = pageIdentity.getSessionId();
    UUID pageId = pageIdentity.getPageId();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoTestSetupUtils.login())
        .get(String.format(SESSION_PAGE_PATH_TEMPLATE, sessionId, pageId))
        .then()
        .statusCode(200)
        .body("data.organizationId", is("000000"))
        .body("data.sessionId", is(sessionId.toString()));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoTestSetupUtils.login())
        .get(String.format(SESSION_PATH_TEMPLATE, sessionId))
        .then()
        .statusCode(200)
        .body("data.organizationId", is("000000"))
        .body("data.deviceId", is(deviceId.toString()))
        .body("data.id", is(sessionId.toString()));

    ObjectNode nextPageNode = objectMapper.readValue(payload, ObjectNode.class);
    nextPageNode.put("deviceId", deviceId.toString());

    dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body(nextPageNode)
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(dataResponse.getData().getDeviceId(), deviceId);
    assertEquals(dataResponse.getData().getSessionId(), sessionId);
    assertNotEquals(dataResponse.getData().getPageId(), pageId);
  }

  @Test
  public void post_page_should_create_new_session_when_no_matching_device_id()
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    DataResponse<PageIdentity> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body(payload)
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    PageIdentity pageIdentity = dataResponse.getData();
    UUID deviceId = pageIdentity.getDeviceId();
    UUID sessionId = pageIdentity.getSessionId();
    UUID pageId = pageIdentity.getPageId();

    dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body(payload)
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertNotEquals(dataResponse.getData().getDeviceId(), deviceId);
    assertNotEquals(dataResponse.getData().getSessionId(), sessionId);
    assertNotEquals(dataResponse.getData().getPageId(), pageId);
  }
}
