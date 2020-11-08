package com.meemaw.auth.sso.oauth.microsoft.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.meemaw.auth.sso.AbstractIdentityProvider;
import com.meemaw.auth.sso.AbstractSsoOAuthResourceTest;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.oauth.AbstractOAuthClient;
import com.meemaw.auth.sso.oauth.AbstractOAuthIdentityProvider;
import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.oauth.microsoft.MicrosoftIdentityProvider;
import com.meemaw.auth.sso.oauth.microsoft.MicrosoftOAuthClient;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.auth.tfa.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.setup.resource.v1.TfaSetupResource;
import com.meemaw.auth.tfa.totp.impl.TotpUtils;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.model.user.User;

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

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class MicrosoftOAuthResourceImplTest extends AbstractSsoOAuthResourceTest {

  @Inject MicrosoftOAuthClient microsoftClient;
  @Inject MicrosoftIdentityProvider microsoftService;

  @Override
  public URI signInUri() {
    return microsoftSignInURI;
  }

  @Override
  public URI callbackUri() {
    return microsoftCallbackURI;
  }

  @Override
  public AbstractIdentityProvider service() {
    return microsoftService;
  }

  @Override
  public AbstractOAuthClient<?, ?, ?> installMockForClient(String email) {
    QuarkusMock.installMockForInstance(new MockedMicrosoftOAuthClient(email), microsoftClient);
    return microsoftClient;
  }

  @Test
  public void microsoft_sign_in__should_use_x_forwarded_headers__when_present() {
    String forwardedProto = "https";
    String forwardedHost = "auth-api.minikube.snuderls.eu";
    String oAuth2CallbackUri =
        forwardedProto
            + "://"
            + forwardedHost
            + MicrosoftOAuthResource.PATH
            + "/"
            + OAuthResource.CALLBACK_PATH;

    String expectedLocationBase =
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id="
            + appConfig.getMicrosoftOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(oAuth2CallbackUri, StandardCharsets.UTF_8)
            + "&response_type=code&scope=openid+email+profile&response_mode=query&state=";

    Response response =
        given()
            .header("X-Forwarded-Proto", forwardedProto)
            .header("X-Forwarded-Host", forwardedHost)
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", SIMPLE_REDIRECT)
            .get(microsoftSignInURI);

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoSignInSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String actualRedirect =
        state.substring(AbstractOAuthIdentityProvider.SECURE_STATE_PREFIX_LENGTH);
    assertEquals(SIMPLE_REDIRECT, URLDecoder.decode(actualRedirect, StandardCharsets.UTF_8));
  }

  @Test
  public void microsoft_sign_in__should_start_flow__by_redirecting_to_provider() {
    String expectedLocationBase =
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id="
            + appConfig.getMicrosoftOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(microsoftCallbackURI.toString(), StandardCharsets.UTF_8)
            + "&response_type=code&scope=openid+email+profile&response_mode=query&state=";

    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("redirect", SIMPLE_REDIRECT)
            .get(microsoftSignInURI);

    response
        .then()
        .statusCode(302)
        .header("Location", startsWith(expectedLocationBase))
        .cookie(SsoSignInSession.COOKIE_NAME);

    String state = response.header("Location").replace(expectedLocationBase, "");
    String actualRedirect =
        state.substring(AbstractOAuthIdentityProvider.SECURE_STATE_PREFIX_LENGTH);
    assertEquals(SIMPLE_REDIRECT, URLDecoder.decode(actualRedirect, StandardCharsets.UTF_8));
  }

  @Test
  public void microsoft_oauth2callback__should_fail__on_random_code() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .get(microsoftCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"AADSTS9002313: Invalid request. Request is malformed or invalid.\"}}"));
  }

  @Test
  public void microsoft_oauth2callback__should_fail__on_expired_code() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam("code", "M.R3_BAY.aff053f8-9755-f5ea-c1b5-a3bb3e4f7b01")
        .queryParam("state", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .get(microsoftCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"AADSTS70000: The provided value for the 'code' parameter is not valid.\"}}"));
  }

  @Test
  public void microsoft_oauth2callback__should_fail__on_invalid_redirect() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));
    String forwardedProto = "https";
    String forwardedHost = "auth-api.minikube.snuderls.eu";

    given()
        .when()
        .queryParam("code", "M.R3_BAY.aff053f8-9755-f5ea-c1b5-a3bb3e4f7b01")
        .queryParam("state", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .header("X-Forwarded-Proto", forwardedProto)
        .header("X-Forwarded-Host", forwardedHost)
        .get(microsoftCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"AADSTS50011: The reply URL specified in the request does not match the reply URLs configured for the application: '783370b6-ee5d-47b5-bc12-2b9ebe4a4f1b'.\"}}"));
  }

  @Test
  public void microsoft_oauth2callback__should_set_verification_cookie__when_user_with_tfa_succeed()
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
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
        .post(TfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(200);

    String Location = "https://www.insight.io/my_path";
    QuarkusMock.installMockForInstance(
        new MockedMicrosoftOAuthClient(user.getEmail()), microsoftClient);
    String state = AbstractIdentityProvider.secureState(Location);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .get(microsoftCallbackURI)
        .then()
        .statusCode(302)
        .header("Location", Location)
        .cookie(SsoChallenge.COOKIE_NAME);
  }

  private static class MockedMicrosoftOAuthClient extends MicrosoftOAuthClient {

    private final String email;

    public MockedMicrosoftOAuthClient(String email) {
      this.email = Objects.requireNonNull(email);
    }

    @Override
    public CompletionStage<MicrosoftTokenResponse> codeExchange(String code, URI redirect) {
      return CompletableFuture.completedStage(new MicrosoftTokenResponse("", "", 1, "", "", ""));
    }

    @Override
    public CompletionStage<MicrosoftUserInfoResponse> userInfo(MicrosoftTokenResponse token) {
      return CompletableFuture.completedStage(
          new MicrosoftUserInfoResponse("", email, "Matej Šnuderl", "Šnuderl", "Matej"));
    }
  }
}
