package com.meemaw.search.core.resource.cors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.nullValue;

import com.meemaw.search.events.resource.EventsResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class CorsTest {

  @Test
  public void returnsAppropriateHeaders_when_allowedOrigin() {
    given()
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", "POST")
        .when().options(EventsResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", "http://localhost:3000")
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", "POST");
  }

  @Test
  public void returnsAppropriateHeaders_when_notAllowedOrigin() {
    given()
        .header("Origin", "http://random.com")
        .header("Access-Control-Request-Method", "POST")
        .when().options(EventsResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", nullValue())
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", "POST");
  }
}
