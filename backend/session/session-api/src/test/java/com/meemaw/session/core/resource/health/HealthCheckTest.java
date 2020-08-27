package com.meemaw.session.core.resource.health;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.test.testconainers.kafka.KafkaTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(KafkaTestResource.class)
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
                "{\"status\":\"UP\",\"checks\":[{\"name\":\"LivenessHealthCheck\",\"status\":\"UP\"},{\"name\":\"SmallRye Reactive Messaging - liveness check\",\"status\":\"UP\",\"data\":{\"events\":\"[OK]\"}},{\"name\":\"ReadinessHealthCheck\",\"status\":\"UP\"},{\"name\":\"SmallRye Reactive Messaging - readiness check\",\"status\":\"UP\",\"data\":{\"events\":\"[OK]\"}},{\"name\":\"Reactive PostgreSQL connection health check\",\"status\":\"UP\"}]}"));
  }

  @Test
  public void liveness() {
    given()
        .when()
        .post("/health/live")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"status\":\"UP\",\"checks\":[{\"name\":\"LivenessHealthCheck\",\"status\":\"UP\"},{\"name\":\"SmallRye Reactive Messaging - liveness check\",\"status\":\"UP\",\"data\":{\"events\":\"[OK]\"}}]}"));
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
                "{\"status\":\"UP\",\"checks\":[{\"name\":\"ReadinessHealthCheck\",\"status\":\"UP\"},{\"name\":\"SmallRye Reactive Messaging - readiness check\",\"status\":\"UP\",\"data\":{\"events\":\"[OK]\"}},{\"name\":\"Reactive PostgreSQL connection health check\",\"status\":\"UP\"}]}"));
  }
}
