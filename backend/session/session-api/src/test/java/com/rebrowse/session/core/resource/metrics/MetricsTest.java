package com.rebrowse.session.core.resource.metrics;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringContains.containsString;

import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class MetricsTest {

  @Test
  public void defaultMetrics() {
    given()
        .when()
        .get("/metrics")
        .then()
        .statusCode(200)
        .body(containsString("base_cpu_processCpuLoad_percent"));
  }

  @Test
  public void applicationMetrics() {
    given().when().get("/" + UUID.randomUUID()).then().statusCode(404);

    given()
        .when()
        .get("/metrics")
        .then()
        .statusCode(200)
        .body(containsString("application_requests_total"))
        .body(containsString("application_request_errors_total{status=\"404\"}"))
        .body(containsString("application_request_client_errors_total{status=\"404\"}"));
  }
}
