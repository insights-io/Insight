package com.meemaw.auth.sso.session.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.resource.v1.SignUpResource;
import com.meemaw.auth.sso.AbstractSsoResourceTest;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.oauth.google.resource.v1.GoogleOAuthResource;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.setup.model.SamlMethod;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupParams;
import com.meemaw.auth.sso.setup.model.dto.SamlConfiguration;
import com.meemaw.auth.sso.setup.resource.v1.SsoSetupResource;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.api.query.QueryParam;
import com.rebrowse.model.auth.UserData;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.model.user.User;
import com.rebrowse.model.user.UserSearchParams;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import io.vertx.core.http.HttpHeaders;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SsoSessionResourceImplTest extends AbstractAuthApiTest {

  @Inject AppConfig appConfig;

  @TestHTTPResource(SamlResource.PATH + "/" + OAuthResource.SIGNIN_PATH)
  URI samlSignInUri;

  @TestHTTPResource(GoogleOAuthResource.PATH + "/" + GoogleOAuthResource.SIGNIN_PATH)
  URI googleSignInUri;

  @TestHTTPResource(GoogleOAuthResource.PATH + "/" + OAuthResource.CALLBACK_PATH)
  URI oauth2CallbackUri;

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  @Test
  public void login__should_fail__when_invalid_content_type() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(SsoSessionResource.PATH + "/login")
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void login__should_fail__when_no_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .post(SsoSessionResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void login__should_fail__when_invalid_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "random")
        .param("password", "random")
        .post(SsoSessionResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\",\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void login__should_fail__when_invalid_credentials() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "test@gmail.com")
        .param("password", "superFancyPassword")
        .header(HttpHeaders.REFERER.toString(), "http://localhost:3000")
        .post(SsoSessionResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }

  @Test
  public void login__should_fail__when_no_referrer() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "test@gmail.com")
        .param("password", "superFancyPassword")
        .post(SsoSessionResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"referer required\"}}"));
  }

  @Test
  public void login__should_fail__when_malformed_referrer() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "test@gmail.com")
        .param("password", "superFancyPassword")
        .header(HttpHeaders.REFERER.toString(), "random")
        .post(SsoSessionResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"no protocol: random\"}}"));
  }

  @Test
  public void login__should_redirect_to_google_sso_provider__when_sso_google_setup()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = password + "@insight-io.com2";
    String sessionId = authApi().signUpAndLogin(email, password);
    CreateSsoSetupParams body = new CreateSsoSetupParams(SsoMethod.GOOGLE, null);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    DataResponse<Void> dataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(
                HttpHeaders.REFERER.toString(),
                "http://localhost:3000/login?redirect=%2Faccount%2Fsettings")
            .post(SsoSessionResource.PATH + "/login")
            .as(new TypeRef<>() {});

    Boom<?> error = dataResponse.getError();
    Map<String, String> errors = (Map<String, String>) error.getErrors();
    assertEquals(400, error.getStatusCode());
    assertEquals("SSO login required", error.getMessage());
    String signInRedirect = Objects.requireNonNull(errors.get("goto"));
    assertEquals(
        signInRedirect,
        UriBuilder.fromUri(googleSignInUri)
            .queryParam("redirect", "http://localhost:3000/account/settings")
            .queryParam("email", email)
            .build()
            .toString());

    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .header(HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .get(URLDecoder.decode(signInRedirect, RebrowseApi.CHARSET))
            .then()
            .statusCode(302)
            .cookie(SsoSignInSession.COOKIE_NAME)
            .extract()
            .response();

    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + appConfig.getGoogleOpenIdClientId()
            + "&redirect_uri="
            + URLEncoder.encode(oauth2CallbackUri.toString(), RebrowseApi.CHARSET)
            + "&response_type=code&scope=openid+email+profile&login_hint="
            + URLEncoder.encode(email, RebrowseApi.CHARSET)
            + "&state=";

    assertThat(response.header("Location"), Matchers.startsWith(expectedLocationBase));
    String state = response.header("Location").replace(expectedLocationBase, "");
    String clientRedirect = state.substring(26);
    assertEquals(
        URLEncoder.encode("http://localhost:3000/account/settings", RebrowseApi.CHARSET),
        clientRedirect);
  }

  @Test
  public void
      login__should_redirect_to_saml_sso_provider_with_correct_redirect__when_sso_saml_setup()
          throws JsonProcessingException, MalformedURLException {
    String password = UUID.randomUUID().toString();
    String email = password + "@insight-io.com";
    String sessionId = authApi().signUpAndLogin(email, password);
    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            new SamlConfiguration(SamlMethod.OKTA, AbstractSsoResourceTest.oktaMetadataEndpoint()));
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    DataResponse<Void> dataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(
                HttpHeaders.REFERER.toString(),
                "http://localhost:3000/login?redirect=/account/settings")
            .post(SsoSessionResource.PATH + "/login")
            .as(new TypeRef<>() {});

    Boom<?> error = dataResponse.getError();
    Map<String, String> errors = (Map<String, String>) error.getErrors();
    assertEquals(400, error.getStatusCode());
    assertEquals("SSO login required", error.getMessage());
    String signInRedirect = Objects.requireNonNull(errors.get("goto"));
    assertEquals(
        signInRedirect,
        UriBuilder.fromUri(samlSignInUri)
            .queryParam("redirect", "http://localhost:3000/account/settings")
            .queryParam("email", email)
            .build()
            .toString());

    given()
        .config(RestAssuredUtils.dontFollowRedirects())
        .when()
        .header(HttpHeaders.REFERER.toString(), "http://localhost:3000")
        .get(URLDecoder.decode(signInRedirect, RebrowseApi.CHARSET))
        .then()
        .statusCode(302)
        .header(
            "Location",
            Matchers.matchesPattern(
                "^https:\\/\\/snuderlstest\\.okta\\.com\\/app\\/snuderlsorg2948061_rebrowse_2\\/exkligrqDovHJsGmk5d5\\/sso\\/saml\\?RelayState=(.*)http%3A%2F%2Flocalhost%3A3000%2Faccount%2Fsettings$"))
        .cookie(SsoSignInSession.COOKIE_NAME);
  }

  @Test
  public void login__should_fail__when_user_with_unfinished_signUp()
      throws JsonProcessingException {
    SignUpRequestDTO signUpRequestDTO =
        authApi().signUpRequestMock("login-no-complete@gmail.com", "password123");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signUpRequestDTO.getEmail())
        .param("password", "superFancyPassword")
        .header(HttpHeaders.REFERER.toString(), "http://localhost:3000")
        .post(SsoSessionResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }

  @Test
  public void logout__should_fail__when_no_cookie() {
    given()
        .when()
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void logout__should_fail_and_clear_cookie__when_random_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"))
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout__should_clear_cookie__when_existing_cookie() throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout__should_clear_session_from_future_sessions_lookups()
      throws JsonProcessingException {
    String email = "test-logoug-sso-sessions@gmail.com";
    String password = "password123";

    String firstSessionId = authApi().signUpAndLogin(email, password);
    String secondSessionId = authApi().login(email, password);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                objectMapper.writeValueAsString(
                    DataResponse.data(List.of(firstSessionId, secondSessionId)))));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(objectMapper.writeValueAsString(DataResponse.data(List.of(firstSessionId)))));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void logout_from_all_devices__should_fail__when_no_cookie() {
    given()
        .when()
        .post(SsoSessionResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void logout_from_all_devices__should_fail__when_random_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(SsoSessionResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout_from_all_devices__should_work__when_existing_session()
      throws JsonProcessingException {
    String email = "test-logout-from-all-devices@gmail.com";
    String password = "test-logout-password";

    String firstSessionId = authApi().signUpAndLogin(email, password);
    String secondSessionId = authApi().login(email, password);

    // Make sure sessions are not the same
    assertNotEquals(firstSessionId, secondSessionId);

    User firstUser =
        UserData.retrieve(authApi().sdkRequest().sessionId(firstSessionId).build())
            .toCompletableFuture()
            .join()
            .getUser();

    User secondUser =
        UserData.retrieve(authApi().sdkRequest().sessionId(secondSessionId).build())
            .toCompletableFuture()
            .join()
            .getUser();

    // Make sure both sessions are associated with same user
    assertEquals(firstUser.getId(), secondUser.getId());
    assertEquals(email, firstUser.getEmail());

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                objectMapper.writeValueAsString(
                    DataResponse.data(List.of(firstSessionId, secondSessionId)))));

    // Logout from all sessions
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .post(SsoSessionResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    // first session is deleted
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoSessionResource.PATH + "/session/userdata")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    // second session id is deleted
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoSessionResource.PATH + "/session/userdata")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void session_should_fail_when_no_sessionId() {
    given()
        .when()
        .get(SsoSessionResource.PATH + "/session/userdata")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void session_should_clear_session_cookie_when_missing_sessionId() {
    given()
        .when()
        .get(SsoSessionResource.PATH + "/session/random/userdata")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void me_should_fail_when_missing_sessionId_cookie() {
    given()
        .when()
        .get(SsoSessionResource.PATH + "/session/userdata")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void me_should_clear_session_cookie_when_missing_sessionId() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .get(SsoSessionResource.PATH + "/session/userdata")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void sso_flow_should_work_with_registered_user() throws JsonProcessingException {
    String email = "sso_flow_test@gmail.com";
    String password = "sso_flow_test_password";
    String sessionId = authApi().signUpAndLogin(email, password);

    User user =
        Organization.members(
                UserSearchParams.builder().email(QueryParam.eq(email)).build(),
                authApi().sdkRequest().sessionId(sessionId).build())
            .toCompletableFuture()
            .join()
            .get(0);

    Organization organization =
        Organization.retrieve(authApi().sdkRequest().sessionId(sessionId).build())
            .toCompletableFuture()
            .join();

    // should be able to get session by id
    UserData userData =
        UserData.retrieve(sessionId, authApi().sdkRequest().build()).toCompletableFuture().join();
    assertEquals(userData, new UserData(user, organization));

    // should be able to get session via cookie
    userData =
        UserData.retrieve(authApi().sdkRequest().sessionId(sessionId).build())
            .toCompletableFuture()
            .join();

    assertEquals(userData, new UserData(user, organization));

    // should be able to logout
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void sessions__should_fail__when_missing_session_id_cookie() {
    given()
        .when()
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void sessions_should_fail_when_random_sessionId_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void sessions__should_return_collection__when_existing_sessionId_cookie()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(SsoSessionResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(sameJson(objectMapper.writeValueAsString(DataResponse.data(List.of(sessionId)))));
  }
}
