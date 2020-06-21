package com.meemaw.auth.core.config.resource;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.test.testconainers.pg.PostgresTestExtension;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class ConfigResourceTest {

  @Test
  public void config() {
    String datasourceURL = PostgresTestExtension.getInstance().getDatasourceURL();

    given()
        .when()
        .get(ConfigResource.PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"datasourceURL\":\"%s\",\"googleOAuthClientId\":\"237859759623-rfpiq8eo37afp0qc294ioqrjtq17q25h.apps.googleusercontent.com\"}",
                    datasourceURL)));
  }
}
