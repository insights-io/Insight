package com.rebrowse.auth.sso.oauth.github.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.zxing.NotFoundException;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.sso.AbstractIdentityProvider;
import com.rebrowse.auth.sso.AbstractSsoOAuthResourceTest;
import com.rebrowse.auth.sso.oauth.AbstractOAuthClient;
import com.rebrowse.auth.sso.oauth.OAuthResource;
import com.rebrowse.auth.sso.oauth.github.GithubIdentityProvider;
import com.rebrowse.auth.sso.oauth.github.GithubOAuthClient;
import com.rebrowse.auth.sso.oauth.github.model.GithubTokenResponse;
import com.rebrowse.auth.sso.oauth.github.model.GithubUserInfoResponse;
import com.rebrowse.model.user.User;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class OpenIdGithubResourceImplTest extends AbstractSsoOAuthResourceTest {

  @Inject GithubOAuthClient oauthClient;
  @Inject GithubIdentityProvider oauthService;

  @Override
  public URI signInUri() {
    return githubSignInURI;
  }

  @Override
  public URI callbackUri() {
    return githubCallbackURI;
  }

  @Override
  public AbstractIdentityProvider service() {
    return oauthService;
  }

  @Override
  public AbstractOAuthClient<?, ?, ?> installMockForClient(String email) {
    QuarkusMock.installMockForInstance(new MockedGithubOAuthClient(email), oauthClient);
    return oauthClient;
  }

  @Test
  public void github_sign_in__should_use_x_forwarded_headers__when_present() {
    String forwardedProto = "https";
    String forwardedHost = "auth-api.minikube.snuderls.eu";
    String oAuth2CallbackUri =
        forwardedProto
            + "://"
            + forwardedHost
            + GithubOAuthResource.PATH
            + "/"
            + OAuthResource.CALLBACK_PATH;

    Response response =
        given()
            .header("X-Forwarded-Proto", forwardedProto)
            .header("X-Forwarded-Host", forwardedHost)
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
            .get(githubSignInURI);

    String expectedLocationBase =
        "https://github.com/login/oauth/authorize?client_id="
            + appConfig.getGithubOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(oAuth2CallbackUri, RebrowseApi.CHARSET)
            + "&response_type=code&scope=read%3Auser+user%3Aemail&state=";

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
  public void github_sign_in__should_start_flow__by_redirecting_to_provider() {
    String expectedLocationBase =
        "https://github.com/login/oauth/authorize?client_id="
            + appConfig.getGithubOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(githubCallbackURI.toString(), RebrowseApi.CHARSET)
            + "&response_type=code&scope=read%3Auser+user%3Aemail&state=";

    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
            .get(githubSignInURI);

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
  public void github_oauth2callback__should_fail__on_random_code() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET));

    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .get(githubCallbackURI)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Bad credentials\"}}"));
  }

  @Test
  public void github_oauth2callback__should_fail__on_expired_code() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET));

    given()
        .when()
        .queryParam("code", "04fc2d3f11120e6ca0e2")
        .queryParam("state", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .get(githubCallbackURI)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Bad credentials\"}}"));
  }

  @Test
  public void github_oauth2callback__should_mfa_challenge__when_totp_setup()
      throws IOException, NotFoundException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    User user = authorizationFlows().retrieveUserData(sessionId).getUser();
    mfaSetupFlows().setupTotpSuccess(user, sessionId);
    QuarkusMock.installMockForInstance(new MockedGithubOAuthClient(user.getEmail()), oauthClient);
    oauthFlows().totpMfaChallenge(githubCallbackURI);
  }

  private static class MockedGithubOAuthClient extends GithubOAuthClient {

    private final String email;

    public MockedGithubOAuthClient(String email) {
      this.email = Objects.requireNonNull(email);
    }

    @Override
    public CompletionStage<GithubTokenResponse> codeExchange(String code, URI redirect) {
      return CompletableFuture.completedStage(new GithubTokenResponse("", "", ""));
    }

    @Override
    public CompletionStage<GithubUserInfoResponse> userInfo(GithubTokenResponse token) {
      return CompletableFuture.completedStage(
          new GithubUserInfoResponse("", "Matej Šnuderl", email, ""));
    }
  }
}
