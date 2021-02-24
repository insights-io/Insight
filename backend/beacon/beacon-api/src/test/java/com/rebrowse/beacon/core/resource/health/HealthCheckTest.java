package com.rebrowse.beacon.core.resource.health;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class HealthCheckTest {

  @Test
  public void health() {
    given()
        .when()
        .post("/q/health")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"status\":\"UP\",\"checks\":[{\"name\":\"SmallRye Reactive Messaging - liveness check\",\"status\":\"UP\",\"data\":{\"events-unload\":\"[OK]\",\"events\":\"[OK]\"}},{\"name\":\"LivenessHealthCheck\",\"status\":\"UP\"},{\"name\":\"SmallRye Reactive Messaging - readiness check\",\"status\":\"UP\",\"data\":{\"events-unload\":\"[OK]\",\"events\":\"[OK]\"}},{\"name\":\"ReadinessHealthCheck\",\"status\":\"UP\"}]}"));
  }

  @Test
  public void liveness() {
    given()
        .when()
        .post("/q/health/live")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"status\":\"UP\",\"checks\":[{\"name\":\"SmallRye Reactive Messaging - liveness check\",\"status\":\"UP\",\"data\":{\"events-unload\":\"[OK]\",\"events\":\"[OK]\"}},{\"name\":\"LivenessHealthCheck\",\"status\":\"UP\"}]}"));
  }

  @Test
  public void readiness() {
    given()
        .when()
        .post("/q/health/ready")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"status\":\"UP\",\"checks\":[{\"name\":\"SmallRye Reactive Messaging - readiness check\",\"status\":\"UP\",\"data\":{\"events-unload\":\"[OK]\",\"events\":\"[OK]\"}},{\"name\":\"ReadinessHealthCheck\",\"status\":\"UP\"}]}"));
  }
}
