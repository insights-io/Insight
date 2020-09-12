package com.meemaw.auth.sso.session.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.login;
import static com.meemaw.test.setup.SsoTestSetupUtils.signUpAndLogin;
import static com.meemaw.test.setup.SsoTestSetupUtils.signUpRequestMock;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.resource.v1.SignUpResource;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.oauth.google.resource.v1.OAuth2GoogleResource;
import com.meemaw.auth.sso.oauth.shared.OAuth2Resource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.resource.v1.SsoSetupResource;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
public class SsoResourceImplTest {

  @Inject MockMailbox mailbox;
  @Inject UserDatasource userDatasource;
  @Inject ObjectMapper objectMapper;
  @Inject AppConfig appConfig;

  @TestHTTPResource(SamlResource.PATH + "/" + SamlResource.SIGNIN_PATH)
  URI samlSignInUri;

  @TestHTTPResource(OAuth2GoogleResource.PATH + "/" + OAuth2GoogleResource.SIGNIN_PATH)
  URI googleSignInUri;

  @TestHTTPResource(OAuth2GoogleResource.PATH + "/" + OAuth2Resource.CALLBACK_PATH)
  URI oauth2CallbackUri;

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  @Test
  public void login_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void login_should_fail_when_no_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void login_should_fail_when_invalid_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "random")
        .param("password", "random")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\",\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void login_should_fail_when_invalid_credentials() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "test@gmail.com")
        .param("password", "superFancyPassword")
        .header("referer", "http://localhost:3000")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }

  @Test
  public void login__should_fail__when_no_referer() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "test@gmail.com")
        .param("password", "superFancyPassword")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"referer required\"}}"));
  }

  @Test
  public void login__should_fail__when_malformed_referer() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "test@gmail.com")
        .param("password", "superFancyPassword")
        .header("referer", "random")
        .post(SsoResource.PATH + "/login")
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
    String sessionId = signUpAndLogin(mailbox, objectMapper, email, password);
    CreateSsoSetupDTO body = new CreateSsoSetupDTO(SsoMethod.GOOGLE, null);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
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
            .header("referer", "http://localhost:3000/login?redirect=/account/settings")
            .post(SsoResource.PATH + "/login")
            .as(new TypeRef<>() {});

    Boom<?> error = dataResponse.getError();
    Map<String, String> errors = (Map<String, String>) error.getErrors();
    assertEquals(400, error.getStatusCode());
    assertEquals("SSO login required", error.getMessage());
    String signInRedirect = Objects.requireNonNull(errors.get("goto"));
    assertEquals(
        signInRedirect,
        UriBuilder.fromUri(googleSignInUri)
            .queryParam("redirect", "/account/settings")
            .queryParam("email", email)
            .build()
            .toString());

    Response response =
        given()
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .header("referer", "http://localhost:3000")
            .get(URLDecoder.decode(signInRedirect, StandardCharsets.UTF_8));
    response.then().statusCode(302).cookie(SsoSignInSession.COOKIE_NAME);

    String oauth2CallbackURL =
        URLEncoder.encode(oauth2CallbackUri.toString(), StandardCharsets.UTF_8);

    String expectedLocationBase =
        "https://accounts.google.com/o/oauth2/auth?client_id="
            + appConfig.getGoogleOpenIdClientId()
            + "&redirect_uri="
            + oauth2CallbackURL
            + "&response_type=code&scope=openid+email+profile&state=";

    assertThat(response.header("Location"), Matchers.startsWith(expectedLocationBase));
    String state = response.header("Location").replace(expectedLocationBase, "");
    String clientRedirect = state.substring(26);
    assertEquals(
        URLEncoder.encode("http://localhost:3000/account/settings", StandardCharsets.UTF_8),
        clientRedirect);
  }

  @Test
  public void
      login__should_redirect_to_saml_sso_provider_with_correct_redirect__when_sso_saml_setup()
          throws JsonProcessingException, MalformedURLException {
    String password = UUID.randomUUID().toString();
    String email = password + "@insight-io.com";
    String sessionId = signUpAndLogin(mailbox, objectMapper, email, password);
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

    DataResponse<Void> dataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000/login?redirect=/account/settings")
            .post(SsoResource.PATH + "/login")
            .as(new TypeRef<>() {});

    Boom<?> error = dataResponse.getError();
    Map<String, String> errors = (Map<String, String>) error.getErrors();
    assertEquals(400, error.getStatusCode());
    assertEquals("SSO login required", error.getMessage());
    String signInRedirect = Objects.requireNonNull(errors.get("goto"));
    assertEquals(
        signInRedirect,
        UriBuilder.fromUri(samlSignInUri)
            .queryParam("redirect", "/account/settings")
            .queryParam("email", email)
            .build()
            .toString());

    given()
        .config(RestAssuredUtils.dontFollowRedirects())
        .when()
        .header("referer", "http://localhost:3000")
        .get(URLDecoder.decode(signInRedirect, StandardCharsets.UTF_8))
        .then()
        .statusCode(302)
        .header(
            "Location",
            Matchers.matchesPattern(
                "^https:\\/\\/snuderls\\.okta\\.com\\/app\\/snuderlsorg446661_insightdev_1\\/exkw843tlucjMJ0kL4x6\\/sso\\/saml\\?RelayState=(.*)http%3A%2F%2Flocalhost%3A3000%2Faccount%2Fsettings$"))
        .cookie(SsoSignInSession.COOKIE_NAME);
  }

  @Test
  public void login_should_fail_when_user_with_unfinished_signUp() throws JsonProcessingException {
    SignUpRequestDTO signUpRequestDTO =
        signUpRequestMock("login-no-complete@gmail.com", "password123");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signUpRequestDTO.getEmail())
        .param("password", "superFancyPassword")
        .header("referer", "http://localhost:3000")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }

  @Test
  public void logout_should_fail_when_no_cookie() {
    given()
        .when()
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void logout_should_fail_and_clear_cookie_on_random_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"))
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout_should_clear_cookie_on_existing_cookie() throws JsonProcessingException {
    String sessionId =
        signUpAndLogin(mailbox, objectMapper, "test-logout@gmail.com", "test-logout-password");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout_should_clear_session_from_future_sessions_lookups()
      throws JsonProcessingException {
    String email = "test-logoug-sso-sessions@gmail.com";
    String password = "password123";

    String firstSessionId = signUpAndLogin(mailbox, objectMapper, email, password);
    String secondSessionId = login(email, password);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                JacksonMapper.get()
                    .writeValueAsString(
                        DataResponse.data(List.of(firstSessionId, secondSessionId)))));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                JacksonMapper.get()
                    .writeValueAsString(DataResponse.data(List.of(firstSessionId)))));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void logout_from_all_devices_should_fail_when_no_cookie() {
    given()
        .when()
        .post(SsoResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sessionId\":\"Required\"}}}"));
  }

  @Test
  public void logout_from_all_devices_should_fail_when_random_cookie() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(SsoResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void logout_from_all_devices_should_work_on_existing_session()
      throws JsonProcessingException {
    String email = "test-logout-from-all-devices@gmail.com";
    String password = "test-logout-password";

    String firstSessionId = signUpAndLogin(mailbox, objectMapper, email, password);
    String secondSessionId = login(email, password);

    // Make sure sessions are not the same
    assertNotEquals(firstSessionId, secondSessionId);

    DataResponse<UserDTO> firstSessionIdUser =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, firstSessionId)
            .get(SsoResource.PATH + "/me")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    DataResponse<UserDTO> secondSessionIdUser =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, secondSessionId)
            .get(SsoResource.PATH + "/me")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    // Make sure both sessions are associated with same user
    assertEquals(firstSessionIdUser, secondSessionIdUser);
    assertEquals(email, firstSessionIdUser.getData().getEmail());

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                JacksonMapper.get()
                    .writeValueAsString(
                        DataResponse.data(List.of(firstSessionId, secondSessionId)))));

    // Logout from all sessions
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .post(SsoResource.PATH + "/logout-from-all-devices")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    // first session is deleted
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, firstSessionId)
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    // second session id is deleted
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void session_should_fail_when_no_sessionId() {
    given()
        .when()
        .get(SsoResource.PATH + "/session")
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
        .queryParam("id", "random")
        .get(SsoResource.PATH + "/session")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void me_should_fail_when_missing_sessionId_cookie() {
    given()
        .when()
        .get(SsoResource.PATH + "/me")
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
        .queryParam("id", "random")
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void sso_flow_should_work_with_registered_user() throws JsonProcessingException {
    String email = "sso_flow_test@gmail.com";
    String password = "sso_flow_test_password";
    String sessionId = signUpAndLogin(mailbox, objectMapper, email, password);

    AuthUser authUser = userDatasource.findUser(email).toCompletableFuture().join().orElseThrow();

    // should be able to get session by id
    given()
        .when()
        .queryParam("id", sessionId)
        .get(SsoResource.PATH + "/session")
        .then()
        .statusCode(200)
        .body(sameJson(objectMapper.writeValueAsString(DataResponse.data(authUser))));

    // should be able to get session via cookie
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(SsoResource.PATH + "/me")
        .then()
        .statusCode(200)
        .body(sameJson(objectMapper.writeValueAsString(DataResponse.data(authUser))));

    // should be able to logout
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");
  }

  @Test
  public void sessions_should_fail_when_missing_sessionId_cookie() {
    given()
        .when()
        .get(SsoResource.PATH + "/sessions")
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
        .get(SsoResource.PATH + "/sessions")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void sessions_should_return_collection_on_existing_sessionId_cookie()
      throws JsonProcessingException {
    String sessionId =
        signUpAndLogin(mailbox, objectMapper, "test-sso-sessions@gmail.com", "password123");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(SsoResource.PATH + "/sessions")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                JacksonMapper.get().writeValueAsString(DataResponse.data(List.of(sessionId)))));
  }
}
