package com.meemaw.rec.resource.v1.beacon;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.rec.beacon.resource.v1.BeaconResource;
import com.meemaw.test.testconainers.api.session.SessionApiTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTestResource(SessionApiTestResource.class)
@QuarkusTest
@Tag("integration")
public class BeaconBeatResourceValidationTest {

  private static final String BEACON_RESOURCE_BEAT_PATH = BeaconResource.PATH + "/beat";

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenNoQueryParams(String contentType) {
    given()
        .when()
        .contentType(contentType)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"organizationId\":\"Required\",\"sessionId\":\"Required\",\"pageId\":\"Required\",\"deviceId\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenEmptyPayload(String contentType) {
    given()
        .when()
        .contentType(contentType)
        .queryParam("deviceId", UUID.randomUUID().toString())
        .queryParam("sessionId", UUID.randomUUID().toString())
        .queryParam("pageId", UUID.randomUUID().toString())
        .queryParam("organizationId", Organization.identifier())
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(422)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"No content to map due to end-of-input\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenEmptyJson(String contentType) {
    given()
        .when()
        .contentType(contentType)
        .queryParam("deviceId", UUID.randomUUID().toString())
        .queryParam("sessionId", UUID.randomUUID().toString())
        .queryParam("pageId", UUID.randomUUID().toString())
        .queryParam("organizationId", Organization.identifier())
        .body("{}")
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sequence\":\"s must be greater than 0\",\"events\":\"e may not be empty\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenNoEvents(String contentType)
      throws IOException, URISyntaxException {
    String body =
        Files.readString(Path.of(getClass().getResource("/beacon/noEvents.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam("deviceId", UUID.randomUUID().toString())
        .queryParam("sessionId", UUID.randomUUID().toString())
        .queryParam("pageId", UUID.randomUUID().toString())
        .queryParam("organizationId", Organization.identifier())
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"events\":\"e may not be empty\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeaconAsJson_shouldThrowError_whenUnlinkedBeacon(String contentType)
      throws IOException, URISyntaxException {
    String body = Files.readString(Path.of(getClass().getResource("/beacon/initial.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam("deviceId", UUID.randomUUID().toString())
        .queryParam("sessionId", UUID.randomUUID().toString())
        .queryParam("pageId", UUID.randomUUID().toString())
        .queryParam("organizationId", Organization.identifier())
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Unlinked beacon\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeaconAsJson_shouldThrowError_invalid_organizationId_length(String contentType)
      throws IOException, URISyntaxException {
    String body = Files.readString(Path.of(getClass().getResource("/beacon/initial.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam("deviceId", UUID.randomUUID().toString())
        .queryParam("sessionId", UUID.randomUUID().toString())
        .queryParam("pageId", UUID.randomUUID().toString())
        .queryParam("organizationId", UUID.randomUUID().toString())
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"organizationId\":\"Has to be 6 characters long\"}}}"));
  }
}
