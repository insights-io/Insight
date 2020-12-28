package com.meemaw.auth.mfa.setup.resource.v1;

import static com.meemaw.auth.utils.AuthApiSetupUtils.getLastSmsMessageVerificationCode;
import static com.meemaw.auth.utils.AuthApiSetupUtils.setupSmsMfa;
import static com.meemaw.auth.utils.AuthApiSetupUtils.setupTotpMfa;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.NotFoundException;
import com.meemaw.auth.mfa.challenge.resource.v1.MfaChallengeResource;
import com.meemaw.auth.mfa.challenge.resource.v1.MfaChallengeResourceImpl;
import com.meemaw.auth.mfa.model.SsoChallenge;
import com.meemaw.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.meemaw.auth.mfa.model.dto.MfaSetupDTO;
import com.meemaw.auth.mfa.totp.datasource.MfaTotpSetupDatasource;
import com.meemaw.auth.mfa.totp.impl.TotpUtils;
import com.meemaw.auth.mfa.totp.model.dto.MfaTotpSetupStartDTO;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.session.resource.v1.SsoSessionResource;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.resource.v1.UserResource;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.shared.sms.impl.mock.MockSmsboxImpl;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.model.organization.OrganizationUpdateParams;
import com.rebrowse.model.user.PhoneNumberUpdateParams;
import com.rebrowse.model.user.User;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Method;
import io.restassured.response.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class MfaSetupResourceImplTest extends AbstractAuthApiTest {

  @Inject MfaTotpSetupDatasource mfaTotpSetupDatasource;
  @Inject MockSmsboxImpl mockSmsbox;

  @ConfigProperty(name = "authorization.issuer")
  String issuer;

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void start_tfa_setup__should_throw__when_unauthorized(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method, "start");
    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, path);
    RestAssuredUtils.challengeSessionCookieTestCases(Method.POST, path);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_not_authenticated(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method, "complete");
    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, path, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, path, ContentType.JSON);
    RestAssuredUtils.challengeSessionCookieTestCases(Method.POST, path, ContentType.JSON);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_invalid_content_type(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method, "complete");
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .post(path)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_no_body(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method, "complete");
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .post(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_empty_body(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method, "complete");
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .body("{}")
        .post(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));
  }

  @Test
  public void complete_totp_tfa_setup__should_throw__when_missing_qr_request()
      throws JsonProcessingException {
    String sessionId = authApi().loginWithAdminUser();

    // 404 before any code is sent
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(10)))
        .post(MfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));
  }

  @Test
  public void list_tfa_setups__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, MfaSetupResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, MfaSetupResource.PATH);
  }

  @Test
  public void list_tfa_setups__should_return_empty_list__when_no_tfa_configured() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(MfaSetupResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));

    String authToken = authApi().createApiKey(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .get(MfaSetupResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }

  @Test
  public void get_tfa_setup__should_throw__when_random_method() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(MfaSetupResource.PATH + "/random")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void get_tfa_setup__should_throw__when_tfa_no_configured(String method) {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(String.join("/", MfaSetupResource.PATH, method))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_tfa_setup__should_throw__when_unauthorized(String method) {
    String path = String.join("/", MfaSetupResource.PATH, method);
    RestAssuredUtils.ssoSessionCookieTestCases(Method.DELETE, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.DELETE, path);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_tfa__should_throw__when_user_without_tfa(String method) {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(String.join("/", MfaSetupResource.PATH, method))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void delete_totp_tfa__should_succeed__when_user_with_tfa()
      throws JsonProcessingException, GeneralSecurityException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    User user = authApi().retrieveUserData(sessionId).getUser();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(MfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(200);

    String secret =
        mfaTotpSetupDatasource.retrieve(user.getId()).toCompletableFuture().join().get();

    int code = TotpUtils.generateCurrentNumber(secret);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(MfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(200);

    // 200 on GET
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(MfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(200);

    // 200 and true on GET
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(MfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(204);

    // 404 on GET after delete
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(MfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void setup_mfa__should_work__on_full_totp_flow()
      throws IOException, GeneralSecurityException, NotFoundException {
    String password = UUID.randomUUID().toString();
    String email = password + "@gmail.com";
    String sessionId = authApi().signUpAndLogin(email, password);
    User user = authApi().retrieveUserData(sessionId).getUser();

    DataResponse<MfaTotpSetupStartDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .post(MfaSetupResource.PATH + "/totp/start")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    String secret =
        mfaTotpSetupDatasource.retrieve(user.getId()).toCompletableFuture().join().get();
    assertEquals(
        String.format("otpauth://totp/%s:%s?secret=%s", issuer, email, secret),
        TotpUtils.readBarcode(dataResponse.getData().getQrImage()).getText());

    // Complete mfa setup fails on invalid code
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(15)))
        .post(MfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    // Complete mfa setup works on valid code
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(
                objectMapper.writeValueAsString(
                    new MfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(secret))))
            .post(MfaSetupResource.PATH + "/totp/complete");

    DataResponse<MfaSetupDTO> responseData = response.as(new TypeRef<>() {});
    response.then().statusCode(200).body(sameJson(objectMapper.writeValueAsString(responseData)));

    // 404 before any code is sent
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(10)))
        .post(MfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(MfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TOTP MFA already set up\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSessionResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(io.vertx.core.http.HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login");

    String challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    response
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\": {\"methods\": [\"totp\"], \"challengeId\": \"%s\"}}", challengeId)))
        .cookie(SsoChallenge.COOKIE_NAME, challengeId);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(10)))
        .post(MfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    Response completeChallengeResponse = completeTotpMfaChallenge(challengeId, secret);

    // new session id from TFA flow
    sessionId = completeChallengeResponse.detailedCookie(SsoSession.COOKIE_NAME).getValue();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(MfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TOTP MFA already set up\"}}"));

    // should clean up verification id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(
            objectMapper.writeValueAsString(
                new MfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(secret))))
        .post(MfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Challenge session expired\"}}"))
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  @Test
  public void complete_sms_mfa_setup__should_throw__when_invalid_auth_identifier()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    PhoneNumberDTO phoneNumber = new PhoneNumberDTO("+386", "51222333");
    String sessionId = authApi().signUpAndLogin(email, password, phoneNumber);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(UserResource.PATH + "/phone_number/verify/send_code")
        .then()
        .statusCode(200);

    int phoneNumberVerificationCode = getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber);
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            objectMapper.writeValueAsString(
                new MfaChallengeCompleteDTO(phoneNumberVerificationCode)))
        .patch(UserResource.PATH + "/phone_number/verify")
        .then()
        .statusCode(200);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(MfaSetupResource.PATH + "/sms/start")
        .then()
        .statusCode(200);

    String secondSessionId = authApi().login(email, password);
    int code = getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, secondSessionId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(MfaSetupResource.PATH + "/sms/complete")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));
  }

  @Test
  public void setup_sms_mfa__should_work__on_full_flow() throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    PhoneNumberDTO phoneNumber = new PhoneNumberDTO("+386", "51222333");
    String sessionId = authApi().signUpAndLogin(email, password, phoneNumber);

    setupSmsMfa(phoneNumber, sessionId, mockSmsbox);
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(io.vertx.core.http.HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login");

    String challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    response
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\": {\"methods\": [\"sms\"], \"challengeId\": \"%s\"}}", challengeId)))
        .cookie(SsoChallenge.COOKIE_NAME, challengeId);

    // 404 before any code is sent
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(123)))
        .post(MfaChallengeResource.PATH + "/sms/complete")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Code expired\"}}"));

    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(MfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"validitySeconds\":60}}"));

    int code = getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(MfaChallengeResourceImpl.PATH + "/sms/complete")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");

    // Should cleanup challenge id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(MfaChallengeResourceImpl.PATH + "/sms/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Challenge session expired\"}}"));
  }

  @Test
  public void setup_mfa__should_work__on_full_sms_and_totp_flow()
      throws JsonProcessingException, GeneralSecurityException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    PhoneNumberDTO phoneNumber = new PhoneNumberDTO("+386", "51222334");
    String sessionId = authApi().signUpAndLogin(email, password, phoneNumber);
    User user = authApi().retrieveUserData(sessionId).getUser();

    setupSmsMfa(phoneNumber, sessionId, mockSmsbox);
    String secret = setupTotpMfa(user.getId(), sessionId, mfaTotpSetupDatasource);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(MfaSetupResource.PATH)
        .then()
        .statusCode(200)
        .body("data.size()", is(2));

    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(io.vertx.core.http.HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login");

    String challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    response
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\": {\"methods\": [\"sms\", \"totp\"], \"challengeId\": \"%s\"}}",
                    challengeId)))
        .cookie(SsoChallenge.COOKIE_NAME, challengeId);

    given()
        .when()
        .get(MfaChallengeResource.PATH + "/" + challengeId)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"sms\",\"totp\"]}"));

    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(MfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"validitySeconds\":60}}"));

    // sms
    int code = getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
        .post(MfaChallengeResourceImpl.PATH + "/sms/complete")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");

    // totp (needs new challenge)
    response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(io.vertx.core.http.HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login");

    challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(
            objectMapper.writeValueAsString(
                new MfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(secret))))
        .post(MfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  @Test
  public void user__should_be_able_to_setup_sms_mfa_and_login__when_mfa_enforced()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    String sessionId = authApi().signUpAndLogin(email, password);

    Organization.update(
            OrganizationUpdateParams.builder().enforceMultiFactorAuthentication(true).build(),
            authApi().sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();

    Cookie challengeCookie =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(io.vertx.core.http.HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login")
            .then()
            .statusCode(200)
            .extract()
            .detailedCookie(SsoChallenge.COOKIE_NAME);

    String challengeId = challengeCookie.getValue();
    assertEquals(SsoChallenge.SIZE, challengeId.length());
    assertEquals(SsoChallenge.TTL, challengeCookie.getMaxAge());

    User updated =
        User.updatePhoneNumber(
                PhoneNumberUpdateParams.builder().countryCode("+386").digits("51123456").build(),
                authApi().sdkRequest().sessionId(sessionId).build())
            .toCompletableFuture()
            .join();

    assertFalse(updated.isPhoneNumberVerified());

    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(MfaSetupResource.PATH + "/sms/start")
        .then()
        .statusCode(200);

    int code = getLastSmsMessageVerificationCode(mockSmsbox, updated.getPhoneNumber().getNumber());
    String newSessionId =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoChallenge.COOKIE_NAME, challengeId)
            .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
            .post(MfaSetupResource.PATH + "/sms/complete/enforced")
            .then()
            .statusCode(200)
            .body(sameJson("{\"data\":true}"))
            .cookie(SsoSession.COOKIE_NAME)
            .cookie(SsoChallenge.COOKIE_NAME, "")
            .extract()
            .detailedCookie(SsoSession.COOKIE_NAME)
            .getValue();

    User sessionUser = authApi().retrieveUserData(newSessionId).getUser();
    assertTrue(sessionUser.isPhoneNumberVerified());
  }

  @Test
  public void user__should_be_able_to_setup_totp_mfa_and_login__when_mfa_enforced()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    String sessionId = authApi().signUpAndLogin(email, password);

    Organization.update(
            OrganizationUpdateParams.builder().enforceMultiFactorAuthentication(true).build(),
            authApi().sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();

    Cookie challengeCookie =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header(io.vertx.core.http.HttpHeaders.REFERER.toString(), "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login")
            .then()
            .statusCode(200)
            .extract()
            .detailedCookie(SsoChallenge.COOKIE_NAME);

    String challengeId = challengeCookie.getValue();
    assertEquals(SsoChallenge.SIZE, challengeId.length());
    assertEquals(SsoChallenge.TTL, challengeCookie.getMaxAge());

    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(MfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(200);

    UUID userId = authApi().retrieveUserData(sessionId).getUser().getId();
    String secret = mfaTotpSetupDatasource.retrieve(userId).toCompletableFuture().join().get();

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              int code = TotpUtils.generateCurrentNumber(secret);
              given()
                  .when()
                  .contentType(MediaType.APPLICATION_JSON)
                  .cookie(SsoChallenge.COOKIE_NAME, challengeId)
                  .body(objectMapper.writeValueAsString(new MfaChallengeCompleteDTO(code)))
                  .post(MfaSetupResource.PATH + "/totp/complete/enforced")
                  .then()
                  .statusCode(200)
                  .body(sameJson("{\"data\":true}"))
                  .cookie(SsoSession.COOKIE_NAME)
                  .cookie(SsoChallenge.COOKIE_NAME, "");
            });
  }

  private Response completeTotpMfaChallenge(String challengeId, String secret) {
    return completeMfaChallenge(
        challengeId,
        () -> {
          try {
            return TotpUtils.generateCurrentNumber(secret);
          } catch (GeneralSecurityException ex) {
            throw new RuntimeException(ex);
          }
        });
  }

  private Response completeMfaChallenge(String challengeId, Supplier<Integer> codeSupplier) {
    AtomicReference<Response> responseWrapper = new AtomicReference<>();
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Response response =
                  given()
                      .when()
                      .contentType(MediaType.APPLICATION_JSON)
                      .cookie(SsoChallenge.COOKIE_NAME, challengeId)
                      .body(
                          objectMapper.writeValueAsString(
                              new MfaChallengeCompleteDTO(codeSupplier.get())))
                      .post(MfaChallengeResourceImpl.PATH + "/totp/complete");

              responseWrapper.set(response);
              response
                  .then()
                  .statusCode(204)
                  .cookie(SsoSession.COOKIE_NAME)
                  .cookie(SsoChallenge.COOKIE_NAME, "");
            });

    return responseWrapper.get();
  }
}
