package com.meemaw.auth.tfa.setup.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.NotFoundException;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.session.resource.v1.SsoSessionResource;
import com.meemaw.auth.tfa.challenge.resource.v1.TfaChallengeResource;
import com.meemaw.auth.tfa.challenge.resource.v1.TfaChallengeResourceImpl;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.auth.tfa.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.model.dto.TfaSetupDTO;
import com.meemaw.auth.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.tfa.totp.impl.TotpUtils;
import com.meemaw.auth.tfa.totp.model.dto.TfaTotpSetupStartDTO;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.utils.AuthApiSetupUtils;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.shared.sms.impl.mock.MockSmsboxImpl;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
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
public class TfaSetupResourceImplTest extends AbstractAuthApiTest {

  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;
  @Inject MockSmsboxImpl mockSmsbox;

  @ConfigProperty(name = "authorization.issuer")
  String issuer;

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void start_tfa_setup__should_throw__when_unauthorized(String method) {
    String path = String.join("/", TfaSetupResource.PATH, method, "start");
    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, path);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_not_authenticated(String method) {
    String path = String.join("/", TfaSetupResource.PATH, method, "complete");
    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, path, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, path, ContentType.JSON);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void complete_tfa_setup__should_throw__when_invalid_content_type(String method) {
    String path = String.join("/", TfaSetupResource.PATH, method, "complete");
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
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
    String path = String.join("/", TfaSetupResource.PATH, method, "complete");
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
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
    String path = String.join("/", TfaSetupResource.PATH, method, "complete");
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
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
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .post(TfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Code expired\"}}"));
  }

  @Test
  public void list_tfa_setups__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, TfaSetupResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, TfaSetupResource.PATH);
  }

  @Test
  public void list_tfa_setups__should_return_empty_list__when_no_tfa_configured() {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaSetupResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .get(TfaSetupResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }

  @Test
  public void get_tfa_setup__should_throw__when_random_method() {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaSetupResource.PATH + "/random")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void get_tfa_setup__should_throw__when_tfa_no_configured(String method) {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(String.join("/", TfaSetupResource.PATH, method))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_tfa_setup__should_throw__when_unauthorized(String method) {
    String path = String.join("/", TfaSetupResource.PATH, method);
    RestAssuredUtils.ssoSessionCookieTestCases(Method.DELETE, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.DELETE, path);
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_tfa__should_throw__when_user_without_tfa(String method) {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(String.join("/", TfaSetupResource.PATH, method))
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
    AuthUser user = authApi().getSessionInfo(sessionId).get().getUser();

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

    // 200 on GET
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(TfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(200);

    // 200 and true on GET
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(TfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(204);

    // 404 on GET after delete
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(TfaSetupResource.PATH + "/totp")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void setup_tfa__should_work__on_full_totp_flow()
      throws IOException, GeneralSecurityException, NotFoundException {
    String password = UUID.randomUUID().toString();
    String email = password + "@gmail.com";
    String sessionId = authApi().signUpAndLogin(email, password);
    AuthUser user = authApi().getSessionInfo(sessionId).get().getUser();

    DataResponse<TfaTotpSetupStartDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .post(TfaSetupResource.PATH + "/totp/start")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    String secret =
        tfaTotpSetupDatasource.getTotpSecret(user.getId()).toCompletableFuture().join().get();
    assertEquals(
        String.format("otpauth://totp/%s:%s?secret=%s", issuer, email, secret),
        TotpUtils.readBarcode(dataResponse.getData().getQrImage()).getText());

    // Complete tfa setup fails on invalid code
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(15)))
        .post(TfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    // Complete tfa setup works on valid code
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(
                objectMapper.writeValueAsString(
                    new TfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(secret))))
            .post(TfaSetupResource.PATH + "/totp/complete");

    DataResponse<TfaSetupDTO> responseData = response.as(new TypeRef<>() {});
    response.then().statusCode(200).body(sameJson(objectMapper.writeValueAsString(responseData)));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .post(TfaSetupResource.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Code expired\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(TfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TOTP TFA already set up\"}}"));

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
            .header("referer", "http://localhost:3000")
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
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .post(TfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    Response completeChallengeResponse = completeTotpTfaChallenge(challengeId, secret);

    // new session id from TFA flow
    sessionId = completeChallengeResponse.detailedCookie(SsoSession.COOKIE_NAME).getValue();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(TfaSetupResource.PATH + "/totp/start")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TOTP TFA already set up\"}}"));

    // should clean up verification id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(
            objectMapper.writeValueAsString(
                new TfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(secret))))
        .post(TfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TFA challenge session expired\"}}"))
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  @Test
  public void setup_tfa__should_work__on_full_sms_flow() throws JsonProcessingException {
    String email = "tfa-sms-full-flow@gmail.com";
    String password = "tfa-sms-full-flow";
    PhoneNumberDTO phoneNumber = new PhoneNumberDTO("+386", "51222333");
    String sessionId = authApi().signUpAndLogin(email, password, phoneNumber);

    AuthApiSetupUtils.setupSmsTfa(phoneNumber, sessionId, mockSmsbox);
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000")
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
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(123)))
        .post(TfaChallengeResource.PATH + "/sms/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(TfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"validitySeconds\":60}}"));

    int code = AuthApiSetupUtils.getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(code)))
        .post(TfaChallengeResourceImpl.PATH + "/sms/complete")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");

    // Should cleanup challenge id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(code)))
        .post(TfaChallengeResourceImpl.PATH + "/sms/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TFA challenge session expired\"}}"));
  }

  @Test
  public void setup_tfa__should_work__on_full_sms_and_totp_flow()
      throws JsonProcessingException, GeneralSecurityException {
    String email = "tfa-sms-and-totp-full-flow@gmail.com";
    String password = "tfa-sms-and-top-full-flow";
    PhoneNumberDTO phoneNumber = new PhoneNumberDTO("+386", "51222334");
    String sessionId = authApi().signUpAndLogin(email, password, phoneNumber);
    AuthUser user = authApi().getSessionInfo(sessionId).get().getUser();

    AuthApiSetupUtils.setupSmsTfa(phoneNumber, sessionId, mockSmsbox);
    String tfaSecret =
        AuthApiSetupUtils.setupTotpTfa(user.getId(), sessionId, tfaTotpSetupDatasource);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaSetupResource.PATH)
        .then()
        .statusCode(200)
        .body("data.size()", is(2));

    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000")
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
        .get(TfaChallengeResource.PATH + "/" + challengeId)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"sms\",\"totp\"]}"));

    given()
        .when()
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .post(TfaChallengeResource.PATH + "/sms/send_code")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"validitySeconds\":60}}"));

    // sms
    int code = AuthApiSetupUtils.getLastSmsMessageVerificationCode(mockSmsbox, phoneNumber);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(objectMapper.writeValueAsString(new TfaChallengeCompleteDTO(code)))
        .post(TfaChallengeResourceImpl.PATH + "/sms/complete")
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
            .header("referer", "http://localhost:3000")
            .post(SsoSessionResource.PATH + "/login");

    challengeId = response.detailedCookie(SsoChallenge.COOKIE_NAME).getValue();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(
            objectMapper.writeValueAsString(
                new TfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(tfaSecret))))
        .post(TfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoChallenge.COOKIE_NAME, "");
  }

  private Response completeTotpTfaChallenge(String challengeId, String secret) {
    return completeTfaChallenge(
        challengeId,
        () -> {
          try {
            return TotpUtils.generateCurrentNumber(secret);
          } catch (GeneralSecurityException ex) {
            throw new RuntimeException(ex);
          }
        });
  }

  private Response completeTfaChallenge(String challengeId, Supplier<Integer> codeSupplier) {
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
                              new TfaChallengeCompleteDTO(codeSupplier.get())))
                      .post(TfaChallengeResourceImpl.PATH + "/totp/complete");

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
