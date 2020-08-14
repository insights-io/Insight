package com.meemaw.auth.sso.resource.v1.google;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.datasource.SsoVerificationDatasource;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.SsoVerification;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
import com.meemaw.auth.sso.model.google.GoogleTokenResponse;
import com.meemaw.auth.sso.model.google.GoogleUserInfoResponse;
import com.meemaw.auth.sso.resource.v1.SsoVerificationResourceImpl;
import com.meemaw.auth.sso.service.google.SsoGoogleClient;
import com.meemaw.auth.sso.service.google.SsoGoogleService;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.setup.SsoTestSetupUtils;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class SsoGoogleResourceImplTest {

  @Inject SsoGoogleClient ssoGoogleClient;
  @Inject SsoGoogleService ssoGoogleService;
  @Inject AppConfig appConfig;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject UserDatasource userDatasource;
  @Inject SsoVerificationDatasource verificationDatasource;

  @TestHTTPResource(SsoGoogleResource.PATH + "/" + SsoGoogleResource.OAUTH2_CALLBACK_PATH)
  URI oauth2CallbackURI;

  @Test
  public void google_sign_in_should_fail_when_no_dest() {
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
  public void google_sign_in_should_fail_when_no_referer() {
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
  public void google_sign_in_should_fail_when_malformed_referer() {
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
  public void google_sign_in_should_use_x_forwarded_headers_when_present() {
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
            + appConfig.getGoogleOAuthClientId()
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
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("dest", dest)
            .get(SsoGoogleResource.PATH + "/signin");

    response.then().statusCode(302).header("Location", startsWith(expectedLocationBase));

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(26);
    assertEquals(URLEncoder.encode(referer + dest, StandardCharsets.UTF_8), destination);
  }

  @Test
  public void google_sign_in_should_start_flow_by_redirecting_to_google() {
    String oauth2CallbackURL =
        URLEncoder.encode(oauth2CallbackURI.toString(), StandardCharsets.UTF_8);

    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + appConfig.getGoogleOAuthClientId()
            + "&redirect_uri="
            + oauth2CallbackURL
            + "&response_type=code&scope=openid+email+profile&state=";

    String referer = "http://localhost:3000";
    String dest = "/test";
    Response response =
        given()
            .header("referer", referer)
            .config(RestAssuredUtils.dontFollowRedirects())
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
        .get(oauth2CallbackURI)
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
        .get(oauth2CallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"invalid_grant. Malformed auth code.\"}}"));
  }

  @Test
  public void google_oauth2callback__should_fail__on_expired_code() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);

    given()
        .when()
        .queryParam(
            "code",
            "4/wwF1aA6SPPRdiJdy95vNLmeFt5237v5juu86VqdJxyR_3VruynuXyXUbFFhtmdGd1jApNM3P3vr8fgGpey-NryM")
        .queryParam("state", state)
        .cookie("state", state)
        .get(oauth2CallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"invalid_grant. Bad Request\"}}"));
  }

  @Test
  public void google_oauth2callback__should_fail__on_invalid_state_cookie() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);

    given()
        .when()
        .queryParam(
            "code",
            "4/wwF1aA6SPPRdiJdy95vNLmeFt5237v5juu86VqdJxyR_3VruynuXyXUbFFhtmdGd1jApNM3P3vr8fgGpey-NryM")
        .queryParam("state", state)
        .get(oauth2CallbackURI)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid state parameter\"}}"));
  }

  @Test
  public void google_oauth2callback__should_set_session_id___when_succeed_with_fresh_sign_up() {
    QuarkusMock.installMockForInstance(
        new MockedSsoGoogleClient("marko.novak+social@gmail.com"), ssoGoogleClient);
    String state = ssoGoogleService.secureState("https://www.insight.io/my_path");

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie("state", state)
        .get(oauth2CallbackURI)
        .then()
        .statusCode(302)
        .header("Location", "https://www.insight.io/my_path")
        .cookie(SsoSession.COOKIE_NAME);
  }

  @Test
  public void google_oauth2callback__should_set_verification_cookie__when_user_with_tfa_succeed()
      throws JsonProcessingException, GeneralSecurityException {
    String email = "sso-login-tfa-full-flow@gmail.com";
    String password = "sso-login-tfa-full-flow";
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);

    DataResponse<Map<String, String>> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(SsoVerificationResourceImpl.PATH + "/setup-tfa")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    String secret =
        verificationDatasource.getTfaSetupSecret(userId).toCompletableFuture().join().get();
    int tfaCode = (int) TimeBasedOneTimePasswordUtil.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(tfaCode)))
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    QuarkusMock.installMockForInstance(new MockedSsoGoogleClient(email), ssoGoogleClient);
    String state = ssoGoogleService.secureState("https://www.insight.io/my_path");

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie("state", state)
        .get(oauth2CallbackURI)
        .then()
        .statusCode(302)
        .header("Location", "https://www.insight.io/my_path")
        .cookie(SsoVerification.COOKIE_NAME);
  }

  private static class MockedSsoGoogleClient extends SsoGoogleClient {

    private final String email;

    public MockedSsoGoogleClient(String email) {
      this.email = Objects.requireNonNull(email);
    }

    @Override
    public CompletionStage<GoogleTokenResponse> codeExchange(String code, String redirectURI) {
      return CompletableFuture.completedStage(new GoogleTokenResponse("", "", "", "", ""));
    }

    @Override
    public CompletionStage<GoogleUserInfoResponse> userInfo(GoogleTokenResponse token) {
      return CompletableFuture.completedStage(
          new GoogleUserInfoResponse(email, "", "", "Matej Šnuderl", "", true, "", ""));
    }
  }
}
