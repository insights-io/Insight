package com.meemaw.rec.resource.v1.beacon;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.rec.beacon.resource.v1.BeaconResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


@QuarkusTest
@Tag("integration")
public class BeaconBeatResourceValidationTest {

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenEmptyPayload(String contentType) {
    given()
        .when().contentType(contentType).post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(422)
        .body(sameJson(
            "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"No content to map due to end-of-input\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenEmptyJson(String contentType) {
    given()
        .when().contentType(contentType).body("{}").post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sequence\":\"s must be greater than 0\",\"events\":\"e may not be empty\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeaconAsJson_shouldThrowError_whenUnlinkedBeacon(String contentType)
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource(
        "/beacon/initial.json").toURI()));

    given()
        .when().contentType(contentType).body(payload).post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Unlinked beacon\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenNoEvents(String contentType)
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource(
        "/beacon/noEvents.json").toURI()));

    given()
        .when().contentType(contentType).body(payload).post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"events\":\"e may not be empty\"}}}"));
  }

}
