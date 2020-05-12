package com.meemaw.auth.core.resource;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.auth.signup.resource.v1.SignupResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class ServiceResourceTest {

  @Test
  public void random_path_should_fail() {
    given()
        .when().post("/" + UUID.randomUUID())
        .then()
        .statusCode(404)
        .body(sameJson("{\"error\":{\"message\":\"Resource Not Found\",\"reason\":\"Not Found\"," +
            "\"statusCode\":404}}"));
  }

  @Test
  public void invalid_method_should_fail() {
    given()
        .when()
        .get(SignupResource.PATH)
        .then()
        .statusCode(405)
        .body(sameJson(
            "{\"error\":{\"message\":\"Method Not Allowed\",\"reason\":\"Method Not Allowed\",\"statusCode\":405}}"));
  }
}
