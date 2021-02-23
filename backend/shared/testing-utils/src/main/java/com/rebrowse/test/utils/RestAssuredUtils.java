package com.rebrowse.test.utils;

import static io.restassured.RestAssured.given;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.test.matchers.SameJSON;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;

public final class RestAssuredUtils {

  private RestAssuredUtils() {}

  public static RestAssuredConfig dontFollowRedirects() {
    return newConfig().redirect(redirectConfig().followRedirects(false));
  }

  public static void bearerTokenExpect401(
      Method method, String path, String token, ContentType contentType) {
    RequestSpecification requestSpecification =
        given().when().header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    if (contentType != null) {
      requestSpecification = requestSpecification.contentType(contentType);
    }

    requestSpecification
        .request(method, path)
        .then()
        .statusCode(401)
        .body(
            SameJSON.sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  public static ValidatableResponse sessionCookieExpect401(
      Method method, String path, String value, ContentType contentType) {
    return cookieExpect401(method, path, SsoSession.COOKIE_NAME, value, contentType);
  }

  public static void ssoSessionCookieTestCases(Method method, String path) {
    ssoSessionCookieTestCases(method, path, null);
  }

  public static void ssoSessionCookieTestCases(
      Method method, String path, ContentType contentType) {
    sessionCookieExpect401(method, path, null, contentType);
    sessionCookieExpect401(method, path, "random", contentType);
    sessionCookieExpect401(method, path, SsoSession.newIdentifier(), contentType);
  }

  public static void challengeSessionCookieTestCases(Method method, String path) {
    challengeSessionCookieTestCases(method, path, null);
  }

  public static void challengeSessionCookieTestCases(
      Method method, String path, ContentType contentType) {
    challengeCookieExpect401(method, path, null, contentType);
    challengeCookieExpect401(method, path, "random", contentType);
    challengeCookieExpect401(
        method, path, AuthorizationPwdChallengeSession.newIdentifier(), contentType);
  }

  public static ValidatableResponse challengeCookieExpect401(
      Method method, String path, String value, ContentType contentType) {
    return cookieExpect401(
        method, path, AuthorizationPwdChallengeSession.COOKIE_NAME, value, contentType);
  }

  public static void ssoBearerTokenTestCases(Method method, String path) {
    ssoBearerTokenTestCases(method, path, null);
  }

  public static void ssoBearerTokenTestCases(Method method, String path, ContentType contentType) {
    bearerTokenExpect401(method, path, null, contentType);
    bearerTokenExpect401(method, path, "random", contentType);
    bearerTokenExpect401(method, path, UUID.randomUUID().toString(), contentType);
  }

  public static ValidatableResponse cookieExpect401(
      Method method, String path, String name, String value, ContentType contentType) {
    RequestSpecification requestSpecification = given().when().cookie(name, value);

    if (contentType != null) {
      requestSpecification = requestSpecification.contentType(contentType);
    }

    ValidatableResponse validatableResponse =
        requestSpecification
            .request(method, path)
            .then()
            .statusCode(401)
            .body(
                SameJSON.sameJson(
                    "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));

    if (Optional.ofNullable(value).orElse("").isEmpty()) {
      return validatableResponse;
    }

    return validatableResponse.cookie(name, "");
  }
}
