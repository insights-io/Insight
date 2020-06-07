package com.meemaw.rec.core.resource.cors;

import static io.restassured.RestAssured.given;

import com.meemaw.rec.beacon.resource.v1.BeaconResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@Tag("integration")
public class CorsTest {

  @ParameterizedTest
  @ValueSource(strings = {"POST", "OPTIONS"})
  public void returnsAppropriateHeaders_when_knownOrigin(String method) {
    given()
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", method)
        .when()
        .options(BeaconResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", "http://localhost:3000")
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", method);
  }

  @ParameterizedTest
  @ValueSource(strings = {"POST", "OPTIONS"})
  public void returnsAppropriateHeaders_when_randomOrigin(String method) {
    given()
        .header("Origin", "http://random.com")
        .header("Access-Control-Request-Method", method)
        .when()
        .options(BeaconResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", "http://random.com")
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", method);
  }
}
