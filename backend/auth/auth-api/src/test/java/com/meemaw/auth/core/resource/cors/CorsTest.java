package com.meemaw.auth.core.resource.cors;

import static io.restassured.RestAssured.given;

import com.meemaw.auth.user.resource.v1.UserResource;
import com.meemaw.shared.SharedConstants;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@Tag("integration")
public class CorsTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "http://localhost:3000",
        "https://app." + SharedConstants.REBROWSE_STAGING_DOMAIN,
        "https://www.app." + SharedConstants.REBROWSE_STAGING_DOMAIN,
        "https://try." + SharedConstants.REBROWSE_STAGING_DOMAIN,
        "https://www.try." + SharedConstants.REBROWSE_STAGING_DOMAIN
      })
  public void returns_appropriate_headers__when_known_origin(String origin) {
    given()
        .header("Origin", origin)
        .header("Access-Control-Request-Method", "POST")
        .when()
        .options(UserResource.PATH)
        .then()
        .statusCode(200)
        .header("access-control-allow-origin", origin)
        .header("access-control-allow-credentials", "true")
        .header("access-control-allow-methods", "POST");
  }
}
