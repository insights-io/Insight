package com.rebrowse.beacon.core.resource.openapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringContains.containsString;

import com.rebrowse.beacon.core.App;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class OpenApiTest {

  @Test
  public void openApi() {
    given().when().get("/openapi").then().statusCode(200).body(containsString(App.TITLE));
  }
}
