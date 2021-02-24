package com.rebrowse.auth.sso.oauth.google.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.NotFoundException;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.sso.AbstractIdentityProvider;
import com.rebrowse.auth.sso.AbstractSsoOAuthResourceTest;
import com.rebrowse.auth.sso.oauth.AbstractOAuthClient;
import com.rebrowse.auth.sso.oauth.OAuthResource;
import com.rebrowse.auth.sso.oauth.google.GoogleIdentityProvider;
import com.rebrowse.auth.sso.oauth.google.GoogleOAuthClient;
import com.rebrowse.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.rebrowse.auth.sso.oauth.google.model.GoogleUserInfoResponse;
import com.rebrowse.auth.sso.saml.client.SamlClient;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.utils.AuthApiTestData;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.auth.utils.MockedSamlClient;
import com.rebrowse.model.auth.SamlConfiguration;
import com.rebrowse.model.auth.SsoSetupCreateParams;
import com.rebrowse.model.organization.OrganizationUpdateParams;
import com.rebrowse.model.user.User;
import com.rebrowse.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
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
            + URLEncoder.encode(callbackURI, RebrowseApi.CHARSET)
            + "&response_type=code&scope=openid+email+profile&state=";

    Response response =
        given()
            .header("X-Forwarded-Proto", forwardedProto)
            .header("X-Forwarded-Host", forwardedHost)
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
            .get(GoogleOAuthResource.PATH + "/signin");

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoAuthorizationSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = state.substring(26);
    assertEquals(
        GlobalTestData.LOCALHOST_REDIRECT, URLDecoder.decode(destination, RebrowseApi.CHARSET));
  }

  @Test
  public void google_sign_in_should_start_flow_by_redirecting_to_google() {
    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + appConfig.getGoogleOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(googleCallbackURI.toString(), RebrowseApi.CHARSET)
            + "&response_type=code&scope=openid+email+profile&state=";

    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
            .get(GoogleOAuthResource.PATH + "/signin");

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoAuthorizationSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination = AbstractIdentityProvider.secureStateData(state);
    assertEquals(
        GlobalTestData.LOCALHOST_REDIRECT, URLDecoder.decode(destination, RebrowseApi.CHARSET));
  }

  @Test
  public void google_oauth2callback__should_fail__on_random_code() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET));

    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
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
            URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET));

    given()
        .when()
        .queryParam(
            "code",
            "4/wwF1aA6SPPRdiJdy95vNLmeFt5237v5juu86VqdJxyR_3VruynuXyXUbFFhtmdGd1jApNM3P3vr8fgGpey-NryM")
        .queryParam("state", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
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
    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", UUID.randomUUID())
        .queryParam("state", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .get(googleCallbackURI)
        .then()
        .statusCode(302)
        .header(HttpHeaders.LOCATION, GlobalTestData.LOCALHOST_REDIRECT)
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, "");
  }

  @Test
  public void google_oauth2callback__should_bypass_mfa_challenge__when_totp_setup()
      throws IOException, NotFoundException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    User user = authorizationFlows().retrieveUserData(sessionId).getUser();
    mfaSetupFlows().setupTotpSuccess(user, sessionId);
    QuarkusMock.installMockForInstance(new MockedGoogleOAuthClient(user.getEmail()), googleClient);
    oauthFlows().callbackAuthorization(googleCallbackURI);
  }

  @Test
  public void google_oauth2callback__should_redirect_to_okta__when_saml_sso_setup()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = AuthApiTestUtils.randomBusinessEmail();
    String otherUserEmail = UUID.randomUUID() + "@" + EmailUtils.getDomain(email);

    QuarkusMock.installMockForInstance(new MockedGoogleOAuthClient(otherUserEmail), googleClient);
    QuarkusMock.installMockForType(MockedSamlClient.okta(), SamlClient.class);

    String sessionId = signUpFlows().signUpAndLogin(email, password);
    organizationFlows()
        .update(OrganizationUpdateParams.builder().openMembership(true).build(), sessionId);

    ssoSetupFlows()
        .create(
            SsoSetupCreateParams.saml(
                SamlConfiguration.okta(AuthApiTestData.OKTA_METADATA_ENDPOINT)),
            sessionId);

    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);

    String location =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("code", UUID.randomUUID())
            .queryParam("state", state)
            .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
            .get(googleCallbackURI)
            .then()
            .statusCode(302)
            .extract()
            .header(HttpHeaders.LOCATION);

    assertThat(location, Matchers.matchesRegex(AuthApiTestData.OKTA_AUTHORIZE_ENDPOINT_PATTERN));
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
