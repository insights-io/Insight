package com.meemaw.auth.core.resource.cors;

import static io.restassured.RestAssured.given;

import com.meemaw.auth.sso.resource.v1.SsoResource;
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
        .options(SsoResource.PATH + "/login")
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", "http://localhost:3000")
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", method);
  }
}
