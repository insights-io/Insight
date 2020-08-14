package com.meemaw.auth.verification.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static com.meemaw.test.setup.SsoTestSetupUtils.signUpAndLogin;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import com.meemaw.auth.sso.datasource.SsoVerificationDatasource;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.SsoVerification;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
import com.meemaw.auth.sso.model.dto.TfaSetupStartDTO;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.sso.resource.v1.SsoVerificationResourceImpl;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.dto.TfaSetupDTO;
import com.meemaw.auth.user.resource.v1.UserTfaResource;
import com.meemaw.shared.io.IoUtils;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SsoVerificationResourceImplTest {

  @Inject UserDatasource userDatasource;
  @Inject SsoVerificationDatasource verificationDatasource;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject UserTfaDatasource userTfaDatasource;

  @Test
  public void get_setup_tfa__should_throw__when_not_authenticated() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .get(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void post_setup_tfa__should_throw__when_not_authenticated() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void post_setup_tfa__should_throw__when_invalid_content_type() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void post_setup_tfa__should_throw__when_no_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void post_setup_tfa__should_throw__when_empty_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body("{}")
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));
  }

  @Test
  public void setup_tfa_complete__should_throw__when_missing_qr_request()
      throws JsonProcessingException {
    String sessionId =
        signUpAndLogin(
            mailbox,
            objectMapper,
            "setup-tfa-missing-qa-request@gmail.com",
            "setup-tfa-missing-qa-request");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(10)))
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"QR code expired\"}}"));
  }

  @Test
  public void complete_tfa__should_throw__when_invalid_content_type() {
    given()
        .when()
        .post(SsoVerificationResourceImpl.PATH + "/complete-tfa")
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void complete_tfa__should_throw__when_no_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(SsoVerificationResourceImpl.PATH + "/complete-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\",\"verificationId\":\"Required\"}}}"));
  }

  @Test
  public void complete_tfa__should_throw__when_empty_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(SsoVerificationResourceImpl.PATH + "/complete-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\",\"verificationId\":\"Required\"}}}"));
  }

  @Test
  public void complete_tfa__should_throw__when_missing_verification_id_cookie()
      throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(10)))
        .post(SsoVerificationResourceImpl.PATH + "/complete-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"verificationId\":\"Required\"}}}"));
  }

  @Test
  public void setup_tfa__should_work__on_full_flow() throws IOException, GeneralSecurityException {
    String email = "setup-tfa-full-flow@gmail.com";
    String password = "setup-tfa-full-flow";
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);

    DataResponse<TfaSetupStartDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(UserTfaResource.PATH + "/setup")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    String secret =
        verificationDatasource.getTfaSetupSecret(userId).toCompletableFuture().join().get();
    String qrImage = dataResponse.getData().getQrImage();

    String expectedQrImageUrl =
        "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/Insight:"
            + email
            + "?secret="
            + secret
            + "&issuer=Insight";

    assertEquals(IoUtils.base64encodeImage(expectedQrImageUrl), qrImage);

    // Fails on invalid code
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(15)))
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid code\"}}"));

    int tfaCode = (int) TimeBasedOneTimePasswordUtil.generateCurrentNumber(secret);

    // Works on valid code
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(tfaCode)))
            .post(UserTfaResource.PATH + "/setup");

    DataResponse<TfaSetupDTO> responseData = response.as(new TypeRef<>() {});
    response
        .then()
        .statusCode(200)
        .body(sameJson(JacksonMapper.get().writeValueAsString(responseData)));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(10)))
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"QR code expired\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TFA already set up\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoResource.PATH + "/logout")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME, "");

    response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .post(SsoResource.PATH + "/login");

    String verificationId = response.detailedCookie(SsoVerification.COOKIE_NAME).getValue();

    response
        .then()
        .statusCode(200)
        .body(sameJson(String.format("{\"data\": {\"verificationId\": \"%s\"}}", verificationId)))
        .cookie(SsoVerification.COOKIE_NAME, verificationId);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoVerification.COOKIE_NAME, verificationId)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(10)))
        .post(SsoVerificationResourceImpl.PATH + "/complete-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));

    response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoVerification.COOKIE_NAME, verificationId)
            .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(tfaCode)))
            .post(SsoVerificationResourceImpl.PATH + "/complete-tfa");

    response
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoVerification.COOKIE_NAME, "");

    // new session id from TFA flow
    sessionId = response.detailedCookie(SsoSession.COOKIE_NAME).getValue();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TFA already set up\"}}"));

    // should clean up verification id
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoVerification.COOKIE_NAME, verificationId)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(tfaCode)))
        .post(SsoVerificationResourceImpl.PATH + "/complete-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Verification session expired\"}}"))
        .cookie(SsoVerification.COOKIE_NAME, "");
  }
}
