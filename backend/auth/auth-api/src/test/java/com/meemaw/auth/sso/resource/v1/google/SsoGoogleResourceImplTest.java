package com.meemaw.auth.sso.resource.v1.google;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class SsoGoogleResourceImplTest {

  @ConfigProperty(name = "google.oauth.client.id")
  String googleOauthClientId;

  @TestHTTPResource(SsoGoogleResource.PATH + "/" + SsoGoogleResource.OAUTH2_CALLBACK_PATH)
  URI oauth2CallbackURI;

  @Test
  public void google_signIn_should_fail_when_no_dest() {
    given()
        .when()
        .get(SsoGoogleResource.PATH + "/signin")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"destination\":\"Required\"}}}"));
  }

  @Test
  public void google_signIn_should_fail_when_no_referer() {
    given()
        .when()
        .queryParam("dest", "/test")
        .get(SsoGoogleResource.PATH + "/signin")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"referer required\"}}"));
  }

  @Test
  public void google_signIn_should_fail_when_malformed_referer() {
    given()
        .header("referer", "malformed")
        .when()
        .queryParam("dest", "/test")
        .get(SsoGoogleResource.PATH + "/signin")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"no protocol: malformed\"}}"));
  }

  @Test
  public void google_signIn_should_use_x_forwarded_headers_when_present() {
    String forwardedProto = "https";
    String forwardedHost = "auth-api.minikube.snuderls.eu";
    String oAuth2CallbackURL =
        forwardedProto
            + "://"
            + forwardedHost
            + SsoGoogleResource.PATH
            + "/"
            + SsoGoogleResource.OAUTH2_CALLBACK_PATH;

    String encodedOAuth2CallbackURL = URLEncoder.encode(oAuth2CallbackURL, StandardCharsets.UTF_8);
    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + googleOauthClientId
            + "&redirect_uri="
            + encodedOAuth2CallbackURL
            + "&response_type=code&scope=openid+email+profile&state=";

    String referer = "http://localhost:3000";
    String dest = "/test";
    Response response =
        given()
            .header("referer", referer)
            .header("X-Forwarded-Proto", forwardedProto)
            .header("X-Forwarded-Host", forwardedHost)
            .config(newConfig().redirect(redirectConfig().followRedirects(false)))
            .when()
            .queryParam("dest", dest)
            .get(SsoGoogleResource.PATH + "/signin");

    response.then().statusCode(302).header("Location", startsWith(expectedLocationBase));

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(26);
    assertEquals(URLEncoder.encode(referer + dest, StandardCharsets.UTF_8), destination);
  }

  @Test
  public void google_signIn_should_start_flow_by_redirecting_to_google() {
    String oauth2CallbackURL =
        URLEncoder.encode(oauth2CallbackURI.toString(), StandardCharsets.UTF_8);

    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + googleOauthClientId
            + "&redirect_uri="
            + oauth2CallbackURL
            + "&response_type=code&scope=openid+email+profile&state=";

    String referer = "http://localhost:3000";
    String dest = "/test";
    Response response =
        given()
            .header("referer", referer)
            .config(newConfig().redirect(redirectConfig().followRedirects(false)))
            .when()
            .queryParam("dest", dest)
            .get(SsoGoogleResource.PATH + "/signin");

    response.then().statusCode(302).header("Location", startsWith(expectedLocationBase));

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(26);
    assertEquals(URLEncoder.encode(referer + dest, StandardCharsets.UTF_8), destination);
  }

  @Test
  public void google_oauth2callback_should_fail_when_no_params() {
    given()
        .when()
        .get(SsoGoogleResource.PATH + "/oauth2callback")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\",\"state\":\"Required\"}}}"));
  }

  @Test
  public void google_oauth2callback_should_fail_on_random_code() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);
    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie("state", state)
        .get(SsoGoogleResource.PATH + "/oauth2callback")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"invalid_grant. Malformed auth code.\"}}"));
  }

  @Test
  public void google_oauth2callback_should_fail_on_expired_code() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);

    given()
        .when()
        .queryParam(
            "code",
            "4/wwF1aA6SPPRdiJdy95vNLmeFt5237v5juu86VqdJxyR_3VruynuXyXUbFFhtmdGd1jApNM3P3vr8fgGpey-NryM")
        .queryParam("state", state)
        .cookie("state", state)
        .get(SsoGoogleResource.PATH + "/oauth2callback")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"invalid_grant. Bad Request\"}}"));
  }

  @Test
  public void google_oauth2callback_should_fail_on_state_cookie() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);

    given()
        .when()
        .queryParam(
            "code",
            "4/wwF1aA6SPPRdiJdy95vNLmeFt5237v5juu86VqdJxyR_3VruynuXyXUbFFhtmdGd1jApNM3P3vr8fgGpey-NryM")
        .queryParam("state", state)
        .get(SsoGoogleResource.PATH + "/oauth2callback")
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid state parameter\"}}"));
  }
}
