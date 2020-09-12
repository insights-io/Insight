package com.meemaw.auth.sso.oauth.google.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.oauth.google.OAuth2GoogleClient;
import com.meemaw.auth.sso.oauth.google.OAuth2GoogleService;
import com.meemaw.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.meemaw.auth.sso.oauth.google.model.GoogleUserInfoResponse;
import com.meemaw.auth.sso.oauth.microsoft.resource.v1.OAuth2MicrosoftResource;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Service;
import com.meemaw.auth.sso.oauth.shared.OAuth2Resource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.resource.v1.SsoSetupResource;
import com.meemaw.auth.sso.tfa.challenge.model.SsoChallenge;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.setup.resource.v1.TfaResource;
import com.meemaw.auth.sso.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.sso.tfa.totp.impl.TotpUtils;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OAuth2GoogleResourceImplTest {

  @Inject OAuth2GoogleClient OAuth2GoogleClient;
  @Inject OAuth2GoogleService OAuth2GoogleService;
  @Inject AppConfig appConfig;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject UserDatasource userDatasource;
  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;

  @TestHTTPResource(OAuth2GoogleResource.PATH + "/" + OAuth2Resource.CALLBACK_PATH)
  URI googleOauth2CallbackURI;

  @TestHTTPResource(OAuth2MicrosoftResource.PATH + "/" + OAuth2Resource.CALLBACK_PATH)
  URI microsoftOauth2CallbackURI;

  @Test
  public void google_sign_in__should_fail__when_missing_redirect() {
    given()
        .when()
        .get(OAuth2GoogleResource.PATH + "/signin")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\"}}}"));
  }

  @Test
  public void google_sign_in_should_fail_when_no_referer() {
    given()
        .when()
        .queryParam("redirect", "/test")
        .get(OAuth2GoogleResource.PATH + "/signin")
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
        .queryParam("redirect", "/test")
        .get(OAuth2GoogleResource.PATH + "/signin")
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
            + OAuth2GoogleResource.PATH
            + "/"
            + OAuth2Resource.CALLBACK_PATH;

    String encodedOAuth2CallbackURL = URLEncoder.encode(oAuth2CallbackURL, StandardCharsets.UTF_8);
    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + appConfig.getGoogleOpenIdClientId()
            + "&redirect_uri="
            + encodedOAuth2CallbackURL
            + "&response_type=code&scope=openid+email+profile&state=";

    String referer = "http://localhost:3000";
    String redirect = "/test";
    Response response =
        given()
            .header("referer", referer)
            .header("X-Forwarded-Proto", forwardedProto)
            .header("X-Forwarded-Host", forwardedHost)
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", redirect)
            .get(OAuth2GoogleResource.PATH + "/signin");

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoSignInSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(26);
    assertEquals(URLEncoder.encode(referer + redirect, StandardCharsets.UTF_8), destination);
  }

  @Test
  public void google_sign_in_should_start_flow_by_redirecting_to_google() {
    String oauth2CallbackURL =
        URLEncoder.encode(googleOauth2CallbackURI.toString(), StandardCharsets.UTF_8);

    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + appConfig.getGoogleOpenIdClientId()
            + "&redirect_uri="
            + oauth2CallbackURL
            + "&response_type=code&scope=openid+email+profile&state=";

    String referer = "http://localhost:3000";
    String redirect = "/test";
    Response response =
        given()
            .header("referer", referer)
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", redirect)
            .get(OAuth2GoogleResource.PATH + "/signin");

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoSignInSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(26);
    assertEquals(URLEncoder.encode(referer + redirect, StandardCharsets.UTF_8), destination);
  }

  @Test
  public void google_oauth2callback_should_fail_when_no_params() {
    given()
        .when()
        .get(googleOauth2CallbackURI)
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
        .get(googleOauth2CallbackURI)
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
        .get(googleOauth2CallbackURI)
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
        .get(googleOauth2CallbackURI)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid state parameter\"}}"));
  }

  @Test
  public void google_oauth2callback__should_set_session_id___when_succeed_with_fresh_sign_up() {
    QuarkusMock.installMockForInstance(
        new MockedOAuth2GoogleClient("marko.novak+social@gmail.com"), OAuth2GoogleClient);
    String state = OAuth2GoogleService.secureState("https://www.insight.io/my_path");

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie("state", state)
        .get(googleOauth2CallbackURI)
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

    QuarkusMock.installMockForInstance(new MockedOAuth2GoogleClient(email), OAuth2GoogleClient);
    String state = OAuth2GoogleService.secureState("https://www.insight.io/my_path");

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie("state", state)
        .get(googleOauth2CallbackURI)
        .then()
        .statusCode(302)
        .header("Location", "https://www.insight.io/my_path")
        .cookie(SsoChallenge.COOKIE_NAME);
  }

  @Test
  public void google_oauth2callback__should_redirect_to_microsoft__when_microsoft_sso_setup()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = password + "@company.io";
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);

    CreateSsoSetupDTO body = new CreateSsoSetupDTO(SsoMethod.MICROSOFT, null);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    String otherUserEmail = UUID.randomUUID() + "@company.io";
    QuarkusMock.installMockForInstance(
        new MockedOAuth2GoogleClient(otherUserEmail), OAuth2GoogleClient);

    String expectedLocationBase =
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id="
            + appConfig.getMicrosoftOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(microsoftOauth2CallbackURI.toString(), StandardCharsets.UTF_8)
            + "&response_type=code&scope=openid+email+profile&response_mode=query&state=";

    String expectedClientDestination = "https://www.insight.io/my_path";
    String paramState = OAuth2GoogleService.secureState(expectedClientDestination);
    Response response =
        given()
            .when()
            .config(RestAssuredUtils.dontFollowRedirects())
            .queryParam("code", "any")
            .queryParam("state", paramState)
            .cookie("state", paramState)
            .get(googleOauth2CallbackURI);

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoSignInSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String actualClientDestination =
        state.substring(AbstractOAuth2Service.SECURE_STATE_PREFIX_LENGTH);
    assertEquals(
        expectedClientDestination,
        URLDecoder.decode(actualClientDestination, StandardCharsets.UTF_8));
  }

  @Test
  public void
      google_oauth2callback__should_add_user_to_organization__when_organization_google_sso_setup()
          throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String domain = "company.io100";
    String email = String.join("@", password, domain);
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);
    CreateSsoSetupDTO body = new CreateSsoSetupDTO(SsoMethod.GOOGLE, null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    String otherUserEmail = String.join("@", UUID.randomUUID().toString(), domain);
    QuarkusMock.installMockForInstance(
        new MockedOAuth2GoogleClient(otherUserEmail), OAuth2GoogleClient);

    String expectedClientDestination = "https://www.insight.io/my_path";
    String paramState = OAuth2GoogleService.secureState(expectedClientDestination);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", paramState)
        .cookie("state", paramState)
        .get(googleOauth2CallbackURI)
        .then()
        .statusCode(302)
        .cookie(SsoSession.COOKIE_NAME);

    AuthUser firstUser = userDatasource.findUser(email).toCompletableFuture().join().get();
    AuthUser secondUser =
        userDatasource.findUser(otherUserEmail).toCompletableFuture().join().get();

    assertEquals(firstUser.getOrganizationId(), secondUser.getOrganizationId());
  }

  @Test
  public void google_oauth2callback__should_redirect_to_okta__when_saml_sso_setup()
      throws JsonProcessingException, MalformedURLException {
    String password = UUID.randomUUID().toString();
    String domain = "company.io10";
    String email = password + "@" + domain;
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);

    CreateSsoSetupDTO body =
        new CreateSsoSetupDTO(
            SsoMethod.SAML,
            new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    String otherUserEmail = UUID.randomUUID() + "@" + domain;
    QuarkusMock.installMockForInstance(
        new MockedOAuth2GoogleClient(otherUserEmail), OAuth2GoogleClient);

    String expectedClientDestination = "https://www.insight.io/my_path";
    String paramState = OAuth2GoogleService.secureState(expectedClientDestination);
    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("code", "any")
            .queryParam("state", paramState)
            .cookie("state", paramState)
            .get(googleOauth2CallbackURI);

    response.then().statusCode(302).cookie(SsoSignInSession.COOKIE_NAME);

    MatcherAssert.assertThat(
        response.header("Location"),
        Matchers.matchesRegex(
            "^https:\\/\\/snuderls\\.okta\\.com\\/app\\/snuderlsorg446661_insightdev_1\\/exkw843tlucjMJ0kL4x6\\/sso\\/saml\\?RelayState=(.*)https%3A%2F%2Fwww\\.insight\\.io%2Fmy_path$"));
  }

  private static class MockedOAuth2GoogleClient extends OAuth2GoogleClient {

    private final String email;

    public MockedOAuth2GoogleClient(String email) {
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
