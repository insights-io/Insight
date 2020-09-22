package com.meemaw.billing.core.resource.health;

import static com.meemaw.test.matchers.SameJSON.sameJson;
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
        .post("/health")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"status\":\"UP\",\"checks\":[{\"name\":\"Reactive PostgreSQL connection health check\",\"status\":\"UP\"}]}"));
  }

  @Test
  public void liveness() {
    given()
        .when()
        .post("/health/live")
        .then()
        .statusCode(200)
        .body(sameJson("{\"status\":\"UP\",\"checks\":[]}"));
  }

  @Test
  public void readiness() {
    given()
        .when()
        .post("/health/ready")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"status\":\"UP\",\"checks\":[{\"name\":\"Reactive PostgreSQL connection health check\",\"status\":\"UP\"}]}"));
  }
}
