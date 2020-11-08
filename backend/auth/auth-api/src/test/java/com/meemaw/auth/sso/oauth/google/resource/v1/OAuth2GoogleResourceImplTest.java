package com.meemaw.auth.sso.oauth.google.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.AbstractIdentityProvider;
import com.meemaw.auth.sso.AbstractSsoOAuthResourceTest;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.oauth.AbstractOAuthClient;
import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.oauth.google.GoogleIdentityProvider;
import com.meemaw.auth.sso.oauth.google.GoogleOAuthClient;
import com.meemaw.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.meemaw.auth.sso.oauth.google.model.GoogleUserInfoResponse;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.setup.model.SamlMethod;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupParams;
import com.meemaw.auth.sso.setup.model.dto.SamlConfiguration;
import com.meemaw.auth.sso.setup.resource.v1.SsoSetupResource;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.auth.tfa.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.setup.resource.v1.TfaSetupResource;
import com.meemaw.auth.tfa.totp.impl.TotpUtils;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.model.user.User;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.net.MalformedURLException;
import java.net.URI;
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
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OAuth2GoogleResourceImplTest extends AbstractSsoOAuthResourceTest {

  @Inject GoogleOAuthClient googleClient;
  @Inject GoogleIdentityProvider googleService;

  @Override
  public URI signInUri() {
    return googleSignInURI;
  }

  @Override
  public URI callbackUri() {
    return googleCallbackURI;
  }

  @Override
  public AbstractIdentityProvider service() {
    return googleService;
  }

  @Override
  public AbstractOAuthClient<?, ?, ?> installMockForClient(String email) {
    QuarkusMock.installMockForInstance(new MockedGoogleOAuthClient(email), googleClient);
    return googleClient;
  }

  @Test
  public void google_sign_in_should_use_x_forwarded_headers_when_present() {
    String forwardedProto = "https";
    String forwardedHost = "auth-api.minikube.snuderls.eu";
    String callbackURI =
        forwardedProto
            + "://"
            + forwardedHost
            + GoogleOAuthResource.PATH
            + "/"
            + OAuthResource.CALLBACK_PATH;

    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + appConfig.getGoogleOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(callbackURI, StandardCharsets.UTF_8)
            + "&response_type=code&scope=openid+email+profile&state=";

    Response response =
        given()
            .header("X-Forwarded-Proto", forwardedProto)
            .header("X-Forwarded-Host", forwardedHost)
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", SIMPLE_REDIRECT)
            .get(GoogleOAuthResource.PATH + "/signin");

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoSignInSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(26);
    assertEquals(SIMPLE_REDIRECT, URLDecoder.decode(destination, StandardCharsets.UTF_8));
  }

  @Test
  public void google_sign_in_should_start_flow_by_redirecting_to_google() {
    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + appConfig.getGoogleOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(googleCallbackURI.toString(), StandardCharsets.UTF_8)
            + "&response_type=code&scope=openid+email+profile&state=";

    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", SIMPLE_REDIRECT)
            .get(GoogleOAuthResource.PATH + "/signin");

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoSignInSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = AbstractIdentityProvider.secureStateData(state);
    assertEquals(SIMPLE_REDIRECT, URLDecoder.decode(destination, StandardCharsets.UTF_8));
  }

  @Test
  public void google_oauth2callback__should_fail__on_random_code() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .get(googleCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"invalid_grant. Malformed auth code.\"}}"));
  }

  @Test
  public void google_oauth2callback__should_fail__on_expired_code() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam(
            "code",
            "4/wwF1aA6SPPRdiJdy95vNLmeFt5237v5juu86VqdJxyR_3VruynuXyXUbFFhtmdGd1jApNM3P3vr8fgGpey-NryM")
        .queryParam("state", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .get(googleCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"invalid_grant. Bad Request\"}}"));
  }

  @Test
  public void google_oauth2callback__should_set_session_id___when_succeed_with_fresh_sign_up() {
    QuarkusMock.installMockForInstance(
        new MockedGoogleOAuthClient("marko.novak+social@gmail.com"), googleClient);
    String state = AbstractIdentityProvider.secureState("https://www.insight.io/my_path");

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .get(googleCallbackURI)
        .then()
        .statusCode(302)
        .header("Location", "https://www.insight.io/my_path")
        .cookie(SsoSession.COOKIE_NAME);
  }

  @Test
  public void google_oauth2callback__should_set_verification_cookie__when_user_with_tfa_succeed()
      throws JsonProcessingException, GeneralSecurityException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    User user = authApi().getSessionInfo(sessionId).getUser();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(TfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(200);

    String secret =
        tfaTotpSetupDatasource.getTotpSecret(user.getId()).toCompletableFuture().join().get();
    int tfaCode = TotpUtils.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
        .post(TfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(200);

    QuarkusMock.installMockForInstance(new MockedGoogleOAuthClient(user.getEmail()), googleClient);
    String state = googleService.secureState("https://www.insight.io/my_path");

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .get(googleCallbackURI)
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
    String sessionId = authApi().signUpAndLogin(email, password);

    CreateSsoSetupParams body = new CreateSsoSetupParams(SsoMethod.MICROSOFT, null);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    String otherUserEmail = UUID.randomUUID() + "@company.io";
    QuarkusMock.installMockForInstance(new MockedGoogleOAuthClient(otherUserEmail), googleClient);

    String expectedLocationBase =
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id="
            + appConfig.getMicrosoftOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(microsoftCallbackURI.toString(), StandardCharsets.UTF_8)
            + "&response_type=code&scope=openid+email+profile&response_mode=query&login_hint="
            + URLEncoder.encode(otherUserEmail, StandardCharsets.UTF_8)
            + "&state=";

    String redirect = "https://www.insight.io/my_path";
    String paramState = googleService.secureState(redirect);
    Response response =
        given()
            .when()
            .config(RestAssuredUtils.dontFollowRedirects())
            .queryParam("code", "any")
            .queryParam("state", paramState)
            .cookie(SsoSignInSession.COOKIE_NAME, paramState)
            .get(googleCallbackURI);

    // Should redirect to microsoft SSO sign in server url
    response.then().statusCode(302);
    String location = response.header("Location");
    assertEquals(
        UriBuilder.fromUri(microsoftSignInURI)
            .queryParam("redirect", redirect)
            .queryParam("email", otherUserEmail)
            .build()
            .toString(),
        location);

    // Should redirect to microsoft SSO provider
    response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .get(URLDecoder.decode(location, StandardCharsets.UTF_8));
    response.then().statusCode(302).cookie(SsoSignInSession.COOKIE_NAME);
    String state = response.header("Location").replace(expectedLocationBase, "");
    String actualClientDestination = AbstractIdentityProvider.secureStateData(state);
    assertEquals(redirect, URLDecoder.decode(actualClientDestination, StandardCharsets.UTF_8));
  }

  @Test
  public void google_oauth2callback__should_redirect_to_okta__when_saml_sso_setup()
      throws JsonProcessingException, MalformedURLException {
    String password = UUID.randomUUID().toString();
    String domain = "company.io10";
    String email = password + "@" + domain;
    String sessionId = authApi().signUpAndLogin(email, password);

    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML, new SamlConfiguration(SamlMethod.OKTA, oktaMetadataEndpoint()));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    String otherUserEmail = UUID.randomUUID() + "@" + domain;
    QuarkusMock.installMockForInstance(new MockedGoogleOAuthClient(otherUserEmail), googleClient);

    String redirect = "https://www.insight.io/my_path";
    String paramState = AbstractIdentityProvider.secureState(redirect);
    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("code", "any")
            .queryParam("state", paramState)
            .cookie(SsoSignInSession.COOKIE_NAME, paramState)
            .get(googleCallbackURI);

    // Should redirect to SAML SSO server sign in
    response.then().statusCode(302);
    String location = response.header("Location");
    assertEquals(
        UriBuilder.fromUri(samlSignInURI)
            .queryParam("redirect", redirect)
            .queryParam("email", otherUserEmail)
            .build()
            .toString(),
        location);

    response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .get(URLDecoder.decode(location, StandardCharsets.UTF_8));

    // Should redirect to SAML SSO provider
    response.then().statusCode(302).cookie(SsoSignInSession.COOKIE_NAME);
    assertThat(
        response.header("Location"),
        Matchers.matchesRegex(
            "^https:\\/\\/snuderlstest\\.okta\\.com\\/app\\/snuderlsorg2948061_rebrowse_2\\/exkligrqDovHJsGmk5d5\\/sso\\/saml\\?RelayState=(.*)https%3A%2F%2Fwww\\.insight\\.io%2Fmy_path$"));
  }

  private static class MockedGoogleOAuthClient extends GoogleOAuthClient {

    private final String email;

    public MockedGoogleOAuthClient(String email) {
      this.email = Objects.requireNonNull(email);
    }

    @Override
    public CompletionStage<GoogleTokenResponse> codeExchange(String code, URI redirect) {
      return CompletableFuture.completedStage(new GoogleTokenResponse("", "", "", "", ""));
    }

    @Override
    public CompletionStage<GoogleUserInfoResponse> userInfo(GoogleTokenResponse token) {
      return CompletableFuture.completedStage(
          new GoogleUserInfoResponse(email, "", "", "Matej Šnuderl", "", true, "", ""));
    }
  }
}
