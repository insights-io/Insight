package com.meemaw.events.core.resource.cors;

import static io.restassured.RestAssured.given;

import com.meemaw.events.search.resource.EventsResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@Tag("integration")
public class CorsTest {

  @ParameterizedTest
  @ValueSource(
      strings = {"https://app.dev.snuderls.eu", "https://www.google.com", "http://localhost:3000"})
  public void cors_on_create_page_should_be_enabled_for_all_origins(String origin) {
    String path =
        String.join("/", EventsResource.PATH, UUID.randomUUID().toString(), "events", "search");

    List.of("POST", "OPTIONS")
        .forEach(
            method -> {
              given()
                  .header("Origin", origin)
                  .header("Access-Control-Request-Method", method)
                  .when()
                  .options(path)
                  .then()
                  .statusCode(200)
                  .header("access-control-allow-origin", origin)
                  .header("access-control-allow-credentials", "true")
                  .header("access-control-allow-methods", method);
            });
  }
}
