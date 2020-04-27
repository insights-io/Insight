package com.meemaw.rec.resource.other;


import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.rec.beacon.resource.v1.BeaconResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class OtherResourceTest {

  @Test
  public void postPath_shouldThrowError_whenPathNotFound() {
    given()
        .when().post("/" + UUID.randomUUID())
        .then()
        .statusCode(404)
        .body(sameJson("{\"error\":{\"message\":\"Resource Not Found\",\"reason\":\"Not Found\"," +
            "\"statusCode\":404}}"));
  }

  @Test
  public void getPath_shouldThrowError_whenUnsupportedMethod() {
    given()
        .when().get(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(405)
        .body(sameJson(
            "{\"error\":{\"message\":\"Method Not Allowed\",\"reason\":\"Method Not Allowed\",\"statusCode\":405}}"));
  }
}
