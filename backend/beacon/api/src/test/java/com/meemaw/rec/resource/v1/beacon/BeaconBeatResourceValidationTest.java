package com.meemaw.rec.resource.v1.beacon;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.rec.beacon.resource.v1.BeaconResource;
import com.meemaw.shared.auth.Organization;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
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


@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class BeaconBeatResourceValidationTest {

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenNoQueryParams(String contentType) {
    given()
        .when().contentType(contentType).post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg3\":\"pageID required\",\"arg2\":\"UserID required\",\"arg1\":\"SessionID required\",\"arg0\":\"OrgID required\"}}}"));
  }


  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenEmptyPayload(String contentType) {
    given()
        .when().contentType(contentType)
        .queryParam("UserID", UUID.randomUUID().toString())
        .queryParam("SessionID", UUID.randomUUID().toString())
        .queryParam("PageID", UUID.randomUUID().toString())
        .queryParam("OrgID", Organization.identifier())
        .post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(422)
        .body(sameJson(
            "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"No content to map due to end-of-input\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenEmptyJson(String contentType) {
    given()
        .when().contentType(contentType)
        .queryParam("UserID", UUID.randomUUID().toString())
        .queryParam("SessionID", UUID.randomUUID().toString())
        .queryParam("PageID", UUID.randomUUID().toString())
        .queryParam("OrgID", Organization.identifier())
        .body("{}").post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sequence\":\"s must be greater than 0\",\"events\":\"e may not be empty\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeacon_shouldThrowError_whenNoEvents(String contentType)
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource(
        "/beacon/noEvents.json").toURI()));

    given()
        .when().contentType(contentType)
        .queryParam("UserID", UUID.randomUUID().toString())
        .queryParam("SessionID", UUID.randomUUID().toString())
        .queryParam("PageID", UUID.randomUUID().toString())
        .queryParam("OrgID", Organization.identifier())
        .body(payload).post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"events\":\"e may not be empty\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeaconAsJson_shouldThrowError_whenUnlinkedBeacon(String contentType)
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource(
        "/beacon/initial.json").toURI()));

    given()
        .when().contentType(contentType)
        .queryParam("UserID", UUID.randomUUID().toString())
        .queryParam("SessionID", UUID.randomUUID().toString())
        .queryParam("PageID", UUID.randomUUID().toString())
        .queryParam("OrgID", Organization.identifier())
        .body(payload).post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Unlinked beacon\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void postBeaconAsJson_shouldThrowError_invalidOrgIdLength(String contentType)
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource(
        "/beacon/initial.json").toURI()));

    given()
        .when().contentType(contentType)
        .queryParam("UserID", UUID.randomUUID().toString())
        .queryParam("SessionID", UUID.randomUUID().toString())
        .queryParam("PageID", UUID.randomUUID().toString())
        .queryParam("OrgID", UUID.randomUUID().toString())
        .body(payload).post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(400)
        .body(sameJson(
            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg0\":\"size must be between 6 and 6\"}}}"));
  }


}
