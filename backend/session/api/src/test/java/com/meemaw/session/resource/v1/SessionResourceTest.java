package com.meemaw.session.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.shared.auth.SsoSession;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.testconainers.pg.PostgresResource;
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
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@QuarkusTest
@Tag("integration")
@QuarkusTestResource(PostgresResource.class)
public class SessionResourceTest {

  @Inject
  ObjectMapper objectMapper;

  @Test
  public void postPage_shouldThrowError_whenUnsupportedMediaType() {
    given()
        .when().contentType(MediaType.TEXT_PLAIN).post(SessionResource.PATH)
        .then()
        .statusCode(415)
        .body(sameJson(
            "{\"error\":{\"message\":\"Media type not supported.\",\"reason\":\"Unsupported Media Type\",\"statusCode\":415}}"));
  }

  @Test
  public void postPage_shouldThrowError_whenEmptyPayload() {
    given()
        .when().contentType(MediaType.APPLICATION_JSON).post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg0\":\"Payload is required\"}}}"));
  }

  @Test
  public void postPage_shouldThrowError_whenEmptyJson() {
    given()
        .when().contentType(MediaType.APPLICATION_JSON).body("{}")
        .post(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"doctype\":\"may not be null\",\"referrer\":\"may not be null\",\"orgId\":\"may not be null\",\"url\":\"may not be null\"}}}"));
  }

  @Test
  public void postPage_shouldLinkSessions_whenMatchingUids()
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource(
        "/page/simple.json").toURI()));

    DataResponse<PageIdentity> dataResponse = given()
        .when()
        .contentType(ContentType.JSON)
        .body(payload)
        .post(SessionResource.PATH)
        .then()
        .statusCode(200)
        .extract()
        .response()
        .as(new TypeRef<>() {
        });

    PageIdentity pageIdentity = dataResponse.getData();
    UUID uid = pageIdentity.getUid();
    UUID sessionId = pageIdentity.getSessionId();
    UUID pageId = pageIdentity.getPageId();

    ObjectNode nextPageNode = objectMapper.readValue(payload, ObjectNode.class);
    nextPageNode.put("uid", uid.toString());

    dataResponse = given()
        .when()
        .contentType(ContentType.JSON)
        .body(nextPageNode)
        .post(SessionResource.PATH)
        .then()
        .statusCode(200)
        .extract()
        .response()
        .as(new TypeRef<>() {
        });

    assertEquals(dataResponse.getData().getUid(), uid);
    assertEquals(dataResponse.getData().getSessionId(), sessionId);
    assertNotEquals(dataResponse.getData().getPageId(), pageId);
  }

  @Test
  public void postPage_shouldNotLink_whenNoMatchingUids() throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource(
        "/page/simple.json").toURI()));

    DataResponse<PageIdentity> dataResponse = given()
        .when()
        .contentType(ContentType.JSON)
        .body(payload)
        .post(SessionResource.PATH)
        .then()
        .statusCode(200)
        .extract()
        .response()
        .as(new TypeRef<>() {
        });

    PageIdentity pageIdentity = dataResponse.getData();
    UUID uid = pageIdentity.getUid();
    UUID sessionId = pageIdentity.getSessionId();
    UUID pageId = pageIdentity.getPageId();

    dataResponse = given()
        .when()
        .contentType(ContentType.JSON)
        .body(payload)
        .post(SessionResource.PATH)
        .then()
        .statusCode(200)
        .extract()
        .response()
        .as(new TypeRef<>() {
        });

    assertNotEquals(dataResponse.getData().getUid(), uid);
    assertNotEquals(dataResponse.getData().getSessionId(), sessionId);
    assertNotEquals(dataResponse.getData().getPageId(), pageId);
  }

  @Test
  public void countPages_shouldThrowError_whenUnauthenticated() {
    given()
        .when()
        .get(SessionResource.PATH)
        .then()
        .statusCode(401)
        .body(sameJson(
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
        .body(sameJson(
            "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }


  // TODO: this test requires auth service to be running
  @Test
  public void countPages_shouldThrowError_whenInvalidSessionId() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .get(SessionResource.PATH)
        .then()
        .statusCode(401)
        .body(sameJson(
            "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

}
