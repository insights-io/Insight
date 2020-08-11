package com.meemaw.auth.verification.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.INSIGHT_ADMIN_ID;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static com.meemaw.test.setup.SsoTestSetupUtils.signUpAndLogin;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.TFASetupCompleteDTO;
import com.meemaw.auth.verification.datasource.TfaSetupDatasource;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.security.GeneralSecurityException;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SsoVerificationResourceImplTest {

  @Inject TfaSetupDatasource verificationDatasource;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;

  @Test
  public void get_setup_tfa__should_throw__when_not_authenticated() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .get(SsoVerificationResourceImpl.PATH + "/setup-tfa")
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
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
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
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
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
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
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
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));
  }

  @Test
  public void setup_tfa__should_throw__when_missing_qr_request() throws JsonProcessingException {
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
        .body(JacksonMapper.get().writeValueAsString(new TFASetupCompleteDTO(10)))
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"QR code expired\"}}"));
  }

  @Test
  public void setup_tfa__should_work__on_full_flow()
      throws JsonProcessingException, GeneralSecurityException {
    DataResponse<Map<String, String>> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
            .get(SsoVerificationResourceImpl.PATH + "/setup-tfa")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    String secret =
        verificationDatasource
            .getTfaSetupSecret(INSIGHT_ADMIN_ID)
            .toCompletableFuture()
            .join()
            .get();
    String qrImageUrl = dataResponse.getData().get("qrImageUrl");

    assertEquals(
        "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/Insight:admin@insight.io?secret="
            + secret
            + "&issuer=Insight",
        qrImageUrl);

    // Fails on invalid code
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body(JacksonMapper.get().writeValueAsString(new TFASetupCompleteDTO(15)))
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid code\"}}"));

    // Works on valid code
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body(
            JacksonMapper.get()
                .writeValueAsString(
                    new TFASetupCompleteDTO(
                        (int) TimeBasedOneTimePasswordUtil.generateCurrentNumber(secret))))
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body(JacksonMapper.get().writeValueAsString(new TFASetupCompleteDTO(10)))
        .post(SsoVerificationResourceImpl.PATH + "/setup-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"QR code expired\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .get(SsoVerificationResourceImpl.PATH + "/setup-tfa")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"TFA already set up\"}}"));
  }
}
