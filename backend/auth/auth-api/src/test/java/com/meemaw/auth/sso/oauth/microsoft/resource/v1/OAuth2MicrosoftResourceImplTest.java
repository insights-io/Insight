package com.meemaw.auth.sso.oauth.microsoft.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftClient;
import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftService;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Service;
import com.meemaw.auth.sso.oauth.shared.OAuth2Resource;
import com.meemaw.auth.sso.tfa.challenge.model.SsoChallenge;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.setup.resource.v1.TfaResource;
import com.meemaw.auth.sso.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.sso.tfa.totp.impl.TotpUtils;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.setup.SsoTestSetupUtils;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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
public class OAuth2MicrosoftResourceImplTest {

  @Inject AppConfig appConfig;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject OAuth2MicrosoftClient openIdClient;
  @Inject OAuth2MicrosoftService openIdService;
  @Inject UserDatasource userDatasource;
  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;

  @TestHTTPResource(OAuth2MicrosoftResource.PATH + "/" + OAuth2Resource.CALLBACK_PATH)
  URI oauth2CallbackUri;

  @TestHTTPResource(OAuth2MicrosoftResource.PATH + "/signin")
  URI signInUri;

  @Test
  public void sign_in__should_fail__when_no_dest() {
    given()
        .when()
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"destination\":\"Required\"}}}"));
  }

  @Test
  public void sign_in__should_fail__when_no_referer() {
    given()
        .when()
        .queryParam("dest", "/test")
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"referer required\"}}"));
  }

  @Test
  public void sign_in__should_fail__when_malformed_referer() {
    given()
        .header("referer", "malformed")
        .when()
        .queryParam("dest", "/test")
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"no protocol: malformed\"}}"));
  }

  @Test
  public void sign_in__should_use_x_forwarded_headers__when_present() {
    String forwardedProto = "https";
    String forwardedHost = "auth-api.minikube.snuderls.eu";
    String oAuth2CallbackUri =
        forwardedProto
            + "://"
            + forwardedHost
            + OAuth2MicrosoftResource.PATH
            + "/"
            + OAuth2Resource.CALLBACK_PATH;

    String expectedLocationBase =
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id="
            + appConfig.getMicrosoftOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(oAuth2CallbackUri, StandardCharsets.UTF_8)
            + "&response_type=code&scope=openid+email+profile&response_mode=query&state=";

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
            .get(signInUri);

    response.then().statusCode(302).header("Location", startsWith(expectedLocationBase));

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(AbstractOAuth2Service.SECURE_STATE_PREFIX_LENGTH);

    assertEquals(URLEncoder.encode(referer + dest, StandardCharsets.UTF_8), destination);
  }

  @Test
  public void sign_in__should_start_flow__by_redirecting_to_provider() {
    String expectedLocationBase =
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id="
            + appConfig.getMicrosoftOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(oauth2CallbackUri.toString(), StandardCharsets.UTF_8)
            + "&response_type=code&scope=openid+email+profile&response_mode=query&state=";

    String referer = "http://localhost:3000";
    String dest = "/test";
    Response response =
        given()
            .header("referer", referer)
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("dest", dest)
            .get(signInUri);

    response.then().statusCode(302).header("Location", startsWith(expectedLocationBase));

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(AbstractOAuth2Service.SECURE_STATE_PREFIX_LENGTH);
    assertEquals(URLEncoder.encode(referer + dest, StandardCharsets.UTF_8), destination);
  }

  @Test
  public void oauth2callback__should_fail__when_no_params() {
    given()
        .when()
        .get(oauth2CallbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\",\"state\":\"Required\"}}}"));
  }

  @Test
  public void oauth2callback__should_fail__on_random_code() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);
    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie("state", state)
        .get(oauth2CallbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"AADSTS9002313: Invalid request. Request is malformed or invalid.\"}}"));
  }

  @Test
  public void oauth2callback__should_fail__on_expired_code() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);

    given()
        .when()
        .queryParam("code", "M.R3_BAY.aff053f8-9755-f5ea-c1b5-a3bb3e4f7b01")
        .queryParam("state", state)
        .cookie("state", state)
        .get(oauth2CallbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"AADSTS70000: The provided value for the 'code' parameter is not valid. The code has expired.\"}}"));
  }

  @Test
  public void oauth2callback__should_fail__on_invalid_state_cookie() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);

    given()
        .when()
        .queryParam("code", "04fc2d3f11120e6ca0e2")
        .queryParam("state", state)
        .get(oauth2CallbackUri)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid state parameter\"}}"));
  }

  @Test
  public void oauth2callback__should_set_verification_cookie__when_user_with_tfa_succeed()
      throws JsonProcessingException, GeneralSecurityException {
    String email = "sso-microsoft-login-tfa-full-flow@gmail.com";
    String password = "sso-microsoft-login-tfa-full-flow";
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(200);

    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    String secret = tfaTotpSetupDatasource.getTotpSecret(userId).toCompletableFuture().join().get();
    int tfaCode = TotpUtils.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
        .post(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(200);

    String Location = "https://www.insight.io/my_path";
    QuarkusMock.installMockForInstance(new MockedOAuth2MicrosoftClient(email), openIdClient);
    String state = openIdService.secureState(Location);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie("state", state)
        .get(oauth2CallbackUri)
        .then()
        .statusCode(302)
        .header("Location", Location)
        .cookie(SsoChallenge.COOKIE_NAME);
  }

  private static class MockedOAuth2MicrosoftClient extends OAuth2MicrosoftClient {

    private final String email;

    public MockedOAuth2MicrosoftClient(String email) {
      this.email = Objects.requireNonNull(email);
    }

    @Override
    public CompletionStage<MicrosoftTokenResponse> codeExchange(String code, String redirectURI) {
      return CompletableFuture.completedStage(new MicrosoftTokenResponse("", "", 1, "", "", ""));
    }

    @Override
    public CompletionStage<MicrosoftUserInfoResponse> userInfo(MicrosoftTokenResponse token) {
      return CompletableFuture.completedStage(
          new MicrosoftUserInfoResponse("", email, "Matej Šnuderl", "Šnuderl", "Matej"));
    }
  }
}
