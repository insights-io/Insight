package com.meemaw.beacon.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.events.index.UserEventTable;
import com.meemaw.shared.SharedConstants;
import com.meemaw.test.testconainers.api.session.SessionApiTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTestResource(SessionApiTestResource.class)
@QuarkusTest
@Tag("integration")
public class BeaconBeatResourceValidationTest {

  private static final String BEACON_RESOURCE_BEAT_PATH = BeaconResource.PATH + "/beat";

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_beacon__should_throw__when_no_query_params(String contentType) {
    given()
        .when()
        .contentType(contentType)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"organizationId\":\"Required\",\"sessionId\":\"Required\",\"pageVisitId\":\"Required\",\"deviceId\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_beacon__should_throw__when_no_body(String contentType) {
    given()
        .when()
        .contentType(contentType)
        .queryParam(UserEventTable.DEVICE_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.SESSION_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.PAGE_VISIT_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.ORGANIZATION_ID, SharedConstants.REBROWSE_ORGANIZATION_ID)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(422)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"No content to map due to end-of-input\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_beacon__should_throw__when_empty_body(String contentType) {
    given()
        .when()
        .contentType(contentType)
        .queryParam(UserEventTable.DEVICE_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.SESSION_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.PAGE_VISIT_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.ORGANIZATION_ID, SharedConstants.REBROWSE_ORGANIZATION_ID)
        .body("{}")
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sequence\":\"s must be greater than 0\",\"events\":\"e may not be empty\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_beacon__should_throw__when_no_events(String contentType)
      throws IOException, URISyntaxException {
    String body =
        Files.readString(Path.of(getClass().getResource("/beacon/noEvents.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam(UserEventTable.DEVICE_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.SESSION_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.PAGE_VISIT_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.ORGANIZATION_ID, SharedConstants.REBROWSE_ORGANIZATION_ID)
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"events\":\"e may not be empty\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_beacon__should_throw__when_unlinked_beacon(String contentType)
      throws IOException, URISyntaxException {
    String body = Files.readString(Path.of(getClass().getResource("/beacon/initial.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam(UserEventTable.DEVICE_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.SESSION_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.PAGE_VISIT_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.ORGANIZATION_ID, SharedConstants.REBROWSE_ORGANIZATION_ID)
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Unlinked beacon\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_beacon__should_throw__when_invalid_organization_id_length(String contentType)
      throws IOException, URISyntaxException {
    String body = Files.readString(Path.of(getClass().getResource("/beacon/initial.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam(UserEventTable.DEVICE_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.SESSION_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.PAGE_VISIT_ID, UUID.randomUUID().toString())
        .queryParam(UserEventTable.ORGANIZATION_ID, UUID.randomUUID().toString())
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"organizationId\":\"Has to be 6 characters long\"}}}"));
  }
}
