package com.meemaw.auth.sso.oauth.microsoft.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.AbstractIdpService;
import com.meemaw.auth.sso.AbstractSsoOAuth2ResourceTest;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.oauth.OAuth2Resource;
import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftClient;
import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftService;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Service;
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
public class OAuth2MicrosoftResourceImplTest extends AbstractSsoOAuth2ResourceTest {

  @Inject OAuth2MicrosoftClient microsoftClient;
  @Inject OAuth2MicrosoftService microsoftService;

  @Override
  public URI signInUri() {
    return microsoftSignInURI;
  }

  @Override
  public URI callbackUri() {
    return microsoftCallbackURI;
  }

  @Override
  public AbstractIdpService service() {
    return microsoftService;
  }

  @Test
  public void microsoft_sign_in__should_use_x_forwarded_headers__when_present() {
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
    String actualRedirect = state.substring(AbstractOAuth2Service.SECURE_STATE_PREFIX_LENGTH);
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
    String actualRedirect = state.substring(AbstractOAuth2Service.SECURE_STATE_PREFIX_LENGTH);
    assertEquals(SIMPLE_REDIRECT, URLDecoder.decode(actualRedirect, StandardCharsets.UTF_8));
  }

  @Test
  public void microsoft_oauth2callback__should_fail__on_random_code() {
    String state =
        microsoftService.secureState(URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie("state", state)
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
        microsoftService.secureState(URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam("code", "M.R3_BAY.aff053f8-9755-f5ea-c1b5-a3bb3e4f7b01")
        .queryParam("state", state)
        .cookie("state", state)
        .get(microsoftCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"AADSTS70000: The provided value for the 'code' parameter is not valid. The code has expired.\"}}"));
  }

  @Test
  public void microsoft_oauth2callback__should_fail__on_invalid_redirect() {
    String state =
        microsoftService.secureState(URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));
    String forwardedProto = "https";
    String forwardedHost = "auth-api.minikube.snuderls.eu";

    given()
        .when()
        .queryParam("code", "M.R3_BAY.aff053f8-9755-f5ea-c1b5-a3bb3e4f7b01")
        .queryParam("state", state)
        .cookie("state", state)
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
    String email = "sso-microsoft-login-tfa-full-flow@gmail.com";
    String password = "sso-microsoft-login-tfa-full-flow";
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
    QuarkusMock.installMockForInstance(new MockedOAuth2MicrosoftClient(email), microsoftClient);
    String state = microsoftService.secureState(Location);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie("state", state)
        .get(microsoftCallbackURI)
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
