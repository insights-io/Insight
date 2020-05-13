package com.meemaw.session.core.resource.health;


import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

@QuarkusTest
@Tag("integration")
public class HealthCheckTest {

  @Test
  public void health() {
    given()
        .when().post("/health")
        .then()
        .statusCode(200)
        .body(sameJson(
            "{\"status\":\"UP\",\"checks\":[{\"name\":\"LivenessHealthCheck\",\"status\":\"UP\"},{\"name\":\"ReadinessHealthCheck\",\"status\":\"UP\"}]}"));
  }

  @Test
  public void liveness() {
    given()
        .when().post("/health/live")
        .then()
        .statusCode(200)
        .body(sameJson(
            "{\"status\":\"UP\",\"checks\":[{\"name\":\"LivenessHealthCheck\",\"status\":\"UP\"}]}"));
  }

  @Test
  public void readiness() {
    given()
        .when().post("/health/ready")
        .then()
        .statusCode(200)
        .body(sameJson(
            "{\"status\":\"UP\",\"checks\":[{\"name\":\"ReadinessHealthCheck\",\"status\":\"UP\"}]}"));
  }
}
