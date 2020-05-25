package com.meemaw.search.core.resource.cors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.nullValue;

import com.meemaw.search.events.resource.EventsResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@Tag("integration")
public class CorsTest {

  @ParameterizedTest
  @ValueSource(strings = {"POST", "OPTIONS"})
  public void returnsAppropriateHeaders_when_allowedOrigin(String method) {
    given()
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", method)
        .when()
        .options(EventsResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", "http://localhost:3000")
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", method);
  }

  @ParameterizedTest
  @ValueSource(strings = {"POST", "OPTIONS"})
  public void returnsAppropriateHeaders_when_notAllowedOrigin(String method) {
    given()
        .header("Origin", "http://random.com")
        .header("Access-Control-Request-Method", method)
        .when()
        .options(EventsResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", nullValue())
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", method);
  }
}
