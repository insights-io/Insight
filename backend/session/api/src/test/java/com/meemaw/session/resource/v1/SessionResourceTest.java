package com.meemaw.session.resource.v1;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.shared.rest.response.DataResponse;
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

  @Inject ObjectMapper objectMapper;

  @Test
  public void postPage_shouldLinkSessions_whenMatchingUids()
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
    UUID uid = pageIdentity.getUid();
    UUID sessionId = pageIdentity.getSessionId();
    UUID pageId = pageIdentity.getPageId();

    given()
        .when()
        .queryParam("orgID", "RC6GTT")
        .get(String.format("%s/%s/pages/%s", SessionResource.PATH, sessionId, pageId))
        .then()
        .statusCode(200)
        .body("data.orgID", is("RC6GTT"))
        .body("data.uid", is(uid.toString()))
        .body("data.sessionID", is(sessionId.toString()));

    ObjectNode nextPageNode = objectMapper.readValue(payload, ObjectNode.class);
    nextPageNode.put("uid", uid.toString());

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

    assertEquals(dataResponse.getData().getUid(), uid);
    assertEquals(dataResponse.getData().getSessionId(), sessionId);
    assertNotEquals(dataResponse.getData().getPageId(), pageId);
  }

  @Test
  public void postPage_shouldNotLink_whenNoMatchingUids() throws IOException, URISyntaxException {
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
    UUID uid = pageIdentity.getUid();
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

    assertNotEquals(dataResponse.getData().getUid(), uid);
    assertNotEquals(dataResponse.getData().getSessionId(), sessionId);
    assertNotEquals(dataResponse.getData().getPageId(), pageId);
  }
}
