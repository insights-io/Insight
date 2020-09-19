package com.meemaw.test.setup;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;

import com.meemaw.auth.sso.session.model.SsoSession;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.Cookie;
import io.restassured.response.Response;

public final class RestAssuredUtils {

  private RestAssuredUtils() {}

  public static RestAssuredConfig dontFollowRedirects() {
    return newConfig().redirect(redirectConfig().followRedirects(false));
  }

  public static void cookieExpect401(String path, String name, String value) {
    given()
        .when()
        .cookie(name, value)
        .get(path)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  public static void sessionCookieExpect401(String path, String value) {
    cookieExpect401(path, SsoSession.COOKIE_NAME, value);
  }

  public static Cookie extractSessionCookie(Response response) {
    return response.getDetailedCookie(SsoSession.COOKIE_NAME);
  }
}
