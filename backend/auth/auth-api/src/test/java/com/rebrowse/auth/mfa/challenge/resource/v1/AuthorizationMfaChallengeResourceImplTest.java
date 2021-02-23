package com.rebrowse.auth.mfa.challenge.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.NotFoundException;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeResponseDTO;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.model.dto.MfaSetupDTO;
import com.rebrowse.auth.mfa.setup.resource.v1.MfaSetupResource;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.model.user.PhoneNumber;
import com.rebrowse.model.user.User;
import com.rebrowse.shared.rest.response.DataResponse;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class AuthorizationMfaChallengeResourceImplTest extends AbstractAuthApiQuarkusTest {

  @TestHTTPResource(AuthorizationMfaChallengeResource.PATH + "/totp/complete-enforced")
  protected URI completeEnforcedTotpMfaChallengeEndpoint;

  @TestHTTPResource(AuthorizationMfaChallengeResource.PATH + "/totp")
  protected URI completeTotpMfaChallengeEndpoint;

  @TestHTTPResource(AuthorizationMfaChallengeResource.PATH + "/sms/send_code")
  protected URI sendSmsChallengeCodeEndpoint;

  @TestHTTPResource(AuthorizationMfaChallengeResource.PATH + "/sms")
  protected URI completeSmsMfaChallengeEndpoint;

  @ConfigProperty(name = "authorization.issuer")
  String issuer;

  @Test
  public void get_mfa_challenge__should_throw__when_random_id() {
    given()
        .when()
        .get(AuthorizationMfaChallengeResource.PATH + "/" + UUID.randomUUID())
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void get_mfa_challenge__should_return_challenge__when_existing_enforced_id()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();

    // 0. Enforce MFA
    String sessionId = signUpFlows().signUpAndLogin(email, password);
    organizationFlows().enforceMfa(sessionId);

    String pwdChallengeId = authorizationFlows().chooseAccount(email);
    String mfaChallengeId =
        mfaAuthorizationFlows()
            .completePwdChallengeToMfa(email, password, pwdChallengeId)
            .getChallengeId();

    given()
        .when()
        .get(AuthorizationMfaChallengeResource.PATH + "/" + mfaChallengeId)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"methods\":[]}}"));
  }

  @Test
  public void send_sms_mfa_challenge_code__should_throw__when_no_challenge_id() {
    given()
        .when()
        .get(sendSmsChallengeCodeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void send_sms_mfa_challenge_code__should_throw__when_random_challenge_id() {
    given()
        .when()
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, UUID.randomUUID())
        .get(sendSmsChallengeCodeEndpoint)
        .then()
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");
  }

  @Test
  public void complete_totp_mfa_challenge__should_throw__when_no_challenge() {
    given()
        .when()
        .post(completeTotpMfaChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void complete_totp_mfa_challenge_enforced_should_throw__when_no_challenge() {
    given()
        .when()
        .post(completeEnforcedTotpMfaChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void complete_totp_mfa_challenge_should_throw__when_random_challenge() {
    given()
        .when()
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, UUID.randomUUID())
        .post(completeTotpMfaChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");
  }

  @Test
  public void complete_totp_mfa_challenge_enforced_should_throw__when_random_challenge() {
    given()
        .when()
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, UUID.randomUUID())
        .post(completeEnforcedTotpMfaChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");
  }

  @Test
  public void complete_totp_mfa_challenge__should_throw__when_bad_request()
      throws IOException, NotFoundException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();
    String sessionId = signUpFlows().signUpAndLogin(email, password);
    User user = authorizationFlows().retrieveUserData(sessionId).getUser();

    // 0. Setup MFA
    mfaSetupFlows().setupTotpSuccess(user, sessionId);

    // 1. Complete password challenge
    AuthorizationMfaChallengeResponseDTO challengeResponse =
        mfaAuthorizationFlows()
            .completePwdChallengeToMfa(email, password, authorizationFlows().chooseAccount(email));
    assertEquals(challengeResponse.getMethods(), List.of(MfaMethod.TOTP));

    given()
        .when()
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeResponse.getChallengeId())
        .post(completeTotpMfaChallengeEndpoint)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));

    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeResponse.getChallengeId())
        .post(completeTotpMfaChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));

    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeResponse.getChallengeId())
        .body("{}")
        .post(completeTotpMfaChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));

    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeResponse.getChallengeId())
        .body(objectMapper.writeValueAsString(Map.of("code", "random")))
        .post(completeTotpMfaChallengeEndpoint)
        .then()
        .statusCode(422)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"Unprocessable Entity\",\"errors\":{\"code\":\"Cannot deserialize value of type `java.lang.Integer` from String \\\"random\\\": not a valid Integer value\"}}}"));

    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeResponse.getChallengeId())
        .body(objectMapper.writeValueAsString(Map.of("code", 123456)))
        .post(completeTotpMfaChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));
  }

  @Test
  public void password_challenge_totp_mfa_full_flow() throws IOException, NotFoundException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();

    // 0. Enforce MFA
    String sessionId = signUpFlows().signUpAndLogin(email, password);
    organizationFlows().enforceMfa(sessionId);

    // 1. Login flow (enforced MFA)
    String passwordChallengeId = authorizationFlows().chooseAccount(email);
    AuthorizationMfaChallengeResponseDTO challengeResponse =
        mfaAuthorizationFlows().completePwdChallengeToMfa(email, password, passwordChallengeId);
    assertEquals(challengeResponse.getMethods(), Collections.emptyList());
    String mfaChallengeId = challengeResponse.getChallengeId();
    String base64qrImage =
        mfaAuthorizationFlows().startEnforcedTotpSetup(mfaChallengeId).getQrImage();
    String totpSecret = AuthApiTestUtils.getSecretFromQrCode(issuer, base64qrImage, email);
    mfaAuthorizationFlows().completeEnforcedTotpChallenge(totpSecret, mfaChallengeId);

    // 2. Login flow (existing MFA)
    passwordChallengeId = authorizationFlows().chooseAccount(email);
    challengeResponse =
        mfaAuthorizationFlows().completePwdChallengeToMfa(email, password, passwordChallengeId);
    assertEquals(challengeResponse.getMethods(), List.of(MfaMethod.TOTP));
    mfaAuthorizationFlows().completeTotpChallenge(totpSecret, challengeResponse.getChallengeId());
  }

  @Test
  public void complete_sms_mfa_challenge_should_throw__when_no_challenge() {
    given()
        .when()
        .post(completeSmsMfaChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void complete_sms_mfa_challenge_should_throw__when_random_challenge() {
    given()
        .when()
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "random")
        .post(completeSmsMfaChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, "");
  }

  @Test
  public void password_challenge_sms_mfa_full_flow() throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();

    // 0. Enforce MFA
    String sessionId = signUpFlows().signUpAndLogin(email, password);
    organizationFlows().enforceMfa(sessionId);

    // 1. Login flow (enforced MFA; SMS)
    String passwordChallengeId = authorizationFlows().chooseAccount(email);
    AuthorizationMfaChallengeResponseDTO challengeResponse =
        mfaAuthorizationFlows().completePwdChallengeToMfa(email, password, passwordChallengeId);
    assertEquals(challengeResponse.getMethods(), Collections.emptyList());
    mfaAuthorizationFlows().startEnforcedSmsChallengeSetup(challengeResponse.getChallengeId());
    PhoneNumber phoneNumber =
        authorizationFlows().retrieveUserData(sessionId).getUser().getPhoneNumber();
    mfaAuthorizationFlows()
        .completeEnforcedSmsChallenge(challengeResponse.getChallengeId(), phoneNumber.getNumber());

    // 2. Login flow (Existing MFA; SMS)
    passwordChallengeId = authorizationFlows().chooseAccount(email);
    challengeResponse =
        mfaAuthorizationFlows().completePwdChallengeToMfa(email, password, passwordChallengeId);
    assertEquals(challengeResponse.getMethods(), List.of(MfaMethod.SMS));
    mfaAuthorizationFlows().sendSmsChallengeCode(challengeResponse.getChallengeId());
    mfaAuthorizationFlows()
        .completeSmsChallenge(challengeResponse.getChallengeId(), phoneNumber.getNumber());
  }

  @Test
  public void password_challenge_sms_and_totp_mfa_full_flow()
      throws IOException, NotFoundException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();
    String sessionId = signUpFlows().signUpAndLogin(email, password);

    // 0. Setup MFA
    User user = authorizationFlows().retrieveUserData(sessionId).getUser();
    userFlows().verifyPhoneNumber(user, sessionId);
    mfaSetupFlows().setupSmsSuccess(user, sessionId);
    String totpSecret = mfaSetupFlows().setupTotpSuccess(user, sessionId).getRight();

    // 1. Verify methods
    DataResponse<List<MfaSetupDTO>> setupsDataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(MfaSetupResource.PATH)
            .then()
            .statusCode(200)
            .body("data.size()", is(2))
            .extract()
            .as(new TypeRef<>() {});

    List<MfaMethod> expectedMethods =
        setupsDataResponse.getData().stream()
            .map(MfaSetupDTO::getMethod)
            .collect(Collectors.toList());

    // 2. Login flow (SMS)
    String passwordChallengeId = authorizationFlows().chooseAccount(email);
    AuthorizationMfaChallengeResponseDTO challengeResponse =
        mfaAuthorizationFlows().completePwdChallengeToMfa(email, password, passwordChallengeId);
    String mfaChallengeId = challengeResponse.getChallengeId();
    assertEquals(challengeResponse.getMethods(), expectedMethods);
    mfaAuthorizationFlows().sendSmsChallengeCode(mfaChallengeId);
    mfaAuthorizationFlows().completeSmsChallenge(mfaChallengeId, user.getPhoneNumber().getNumber());

    // 3. Login flow (TOTP)
    passwordChallengeId = authorizationFlows().chooseAccount(email);
    challengeResponse =
        mfaAuthorizationFlows().completePwdChallengeToMfa(email, password, passwordChallengeId);
    mfaChallengeId = challengeResponse.getChallengeId();
    assertEquals(challengeResponse.getMethods(), expectedMethods);
    mfaAuthorizationFlows().completeTotpChallenge(totpSecret, mfaChallengeId);
  }
}
