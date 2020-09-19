package com.meemaw.auth.sso.oauth.github.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.AbstractIdpService;
import com.meemaw.auth.sso.AbstractSsoOAuth2ResourceTest;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.oauth.OAuth2Resource;
import com.meemaw.auth.sso.oauth.github.OAuth2GithubClient;
import com.meemaw.auth.sso.oauth.github.OAuth2GithubService;
import com.meemaw.auth.sso.oauth.github.model.GithubTokenResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubUserInfoResponse;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.tfa.challenge.model.SsoChallenge;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.setup.resource.v1.TfaResource;
import com.meemaw.auth.sso.tfa.totp.impl.TotpUtils;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OpenIdGithubResourceImplTest extends AbstractSsoOAuth2ResourceTest {

  @Inject OAuth2GithubClient oauthClient;
  @Inject OAuth2GithubService oauthService;

  @Override
  public URI signInUri() {
    return githubSignInURI;
  }

  @Override
  public URI callbackUri() {
    return githubCallbackURI;
  }

  @Override
  public AbstractIdpService service() {
    return oauthService;
  }

  @Test
  public void github_sign_in__should_use_x_forwarded_headers__when_present() {
    String forwardedProto = "https";
    String forwardedHost = "auth-api.minikube.snuderls.eu";
    String oAuth2CallbackUri =
        forwardedProto
            + "://"
            + forwardedHost
            + OAuth2GithubResource.PATH
            + "/"
            + OAuth2Resource.CALLBACK_PATH;

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
    String destination = oauthService.secureStateData(state);
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
    String destination = oauthService.secureStateData(state);
    assertEquals(SIMPLE_REDIRECT, URLDecoder.decode(destination, StandardCharsets.UTF_8));
  }

  @Test
  public void github_oauth2callback__should_fail__on_random_code() {
    String state =
        oauthService.secureState(URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

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
        oauthService.secureState(URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

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
    String email = "sso-github-login-tfa-full-flow@gmail.com";
    String password = "sso-github-login-tfa-full-flow";
    String sessionId = authApi().signUpAndLogin(email, password);

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
    QuarkusMock.installMockForInstance(new MockedOAuth2GithubClient(email), oauthClient);
    String state = oauthService.secureState(Location);

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

  private static class MockedOAuth2GithubClient extends OAuth2GithubClient {

    private final String email;

    public MockedOAuth2GithubClient(String email) {
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
