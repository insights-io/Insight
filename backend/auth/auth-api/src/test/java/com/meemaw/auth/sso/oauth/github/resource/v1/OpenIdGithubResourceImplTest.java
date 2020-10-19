package com.meemaw.auth.sso.oauth.github.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.AbstractIdentityProvider;
import com.meemaw.auth.sso.AbstractSsoOAuthResourceTest;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.oauth.github.GithubIdentityProvider;
import com.meemaw.auth.sso.oauth.github.GithubOAuthClient;
import com.meemaw.auth.sso.oauth.github.model.GithubTokenResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuthClient;
import com.meemaw.auth.sso.session.model.SsoSession;
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
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
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
            .queryParam("redirect", SIMPLE_REDIRECT)
            .get(githubSignInURI);

    String expectedLocationBase =
        "https://github.com/login/oauth/authorize?client_id="
            + appConfig.getGithubOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(oAuth2CallbackUri, StandardCharsets.UTF_8)
            + "&response_type=code&scope=read%3Auser+user%3Aemail&state=";

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
  public void github_sign_in__should_start_flow__by_redirecting_to_provider() {
    String expectedLocationBase =
        "https://github.com/login/oauth/authorize?client_id="
            + appConfig.getGithubOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(githubCallbackURI.toString(), StandardCharsets.UTF_8)
            + "&response_type=code&scope=read%3Auser+user%3Aemail&state=";

    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", SIMPLE_REDIRECT)
            .get(githubSignInURI);

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
  public void github_oauth2callback__should_fail__on_random_code() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie("state", state)
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
            URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam("code", "04fc2d3f11120e6ca0e2")
        .queryParam("state", state)
        .cookie("state", state)
        .get(githubCallbackURI)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Bad credentials\"}}"));
  }

  @Test
  public void github_oauth2callback__should_set_verification_cookie__when_user_with_tfa_succeed()
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

    String Location = "https://www.insight.io/my_path";
    QuarkusMock.installMockForInstance(new MockedGithubOAuthClient(user.getEmail()), oauthClient);
    String state = AbstractIdentityProvider.secureState(Location);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie("state", state)
        .get(githubCallbackURI)
        .then()
        .statusCode(302)
        .header("Location", Location)
        .cookie(SsoChallenge.COOKIE_NAME);
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
