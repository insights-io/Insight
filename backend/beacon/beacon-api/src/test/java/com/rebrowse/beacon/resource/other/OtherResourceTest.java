package com.rebrowse.beacon.resource.other;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.rebrowse.beacon.resource.v1.RecordingResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
        .get(RecordingResource.PATH + "/beat")
        .then()
        .statusCode(405)
        .body(
            sameJson(
                "{\"error\":{\"message\":\"Method Not Allowed\",\"reason\":\"Method Not Allowed\",\"statusCode\":405}}"));
  }
}
