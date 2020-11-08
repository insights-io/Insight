package com.meemaw.beacon.resource.other;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.meemaw.beacon.resource.v1.BeaconResource;

import java.util.UUID;

@QuarkusTest
@Tag("integration")
public class OtherResourceTest {

  @Test
  public void post_path_should_throw__when_path_not_found() {
    given()
        .when()
        .post("/" + UUID.randomUUID())
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"message\":\"Resource Not Found\",\"reason\":\"Not Found\","
                    + "\"statusCode\":404}}"));
  }

  @Test
  public void get_path_should_throw__when_unsupported_method() {
    given()
        .when()
        .get(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(405)
        .body(
            sameJson(
                "{\"error\":{\"message\":\"Method Not Allowed\",\"reason\":\"Method Not Allowed\",\"statusCode\":405}}"));
  }
}
