package com.meemaw.rec.core.resource.cors;

import static io.restassured.RestAssured.given;

import com.meemaw.rec.beacon.resource.v1.BeaconResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class CorsTest {

  @Test
  public void returnsAppropriateHeaders_when_knownOrigin() {
    given()
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", "POST")
        .when().options(BeaconResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", "http://localhost:3000")
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", "POST");
  }

  @Test
  public void returnsAppropriateHeaders_when_randomOrigin() {
    given()
        .header("Origin", "http://random.com")
        .header("Access-Control-Request-Method", "POST")
        .when().options(BeaconResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", "http://random.com")
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", "POST");
  }
}
