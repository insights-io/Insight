package com.meemaw.auth.user.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.utils.AuthApiSetupUtils;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.shared.sms.MockSmsbox;
import com.meemaw.test.matchers.SameJSON;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class UserResourceImplTest {

  private static final String PHONE_NUMBER_VERIFY_PATH =
      String.join("/", UserResource.PATH, "phone_number", "verify");

  private static final String PHONE_NUMBER_VERIFY_SEND_CODE_PATH =
      String.join("/", PHONE_NUMBER_VERIFY_PATH, "send_code");

  @Inject MockSmsbox mockSmsbox;
  @Inject MockMailbox mockMailbox;
  @Inject ObjectMapper objectMapper;

  @Test
  public void phone_number_verify_send_code__should_throw__when_no_authentication() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .post(PHONE_NUMBER_VERIFY_SEND_CODE_PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void phone_number_verify_send_code__should_throw__when_no_phone_number()
      throws JsonProcessingException {
    String sessionId =
        SsoTestSetupUtils.signUpAndLogin(
            mockMailbox, objectMapper, "user-no-phone-number@gmail.com", "user-12345");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(PHONE_NUMBER_VERIFY_SEND_CODE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_invalid_content_type() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_no_authentication() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_no_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_empty_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body("{}")
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_invalid_code()
      throws JsonProcessingException {
    String sessionId =
        SsoTestSetupUtils.signUpAndLogin(
            mockMailbox,
            objectMapper,
            "phone-number-verify-invalid-code@gmail.com",
            "phone-number-verify-invalid-code",
            "+386512121");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"code\":\"Invalid code\"}}}"));
  }

  @Test
  public void phone_number_verify__should_throw__when_no_phone_number()
      throws JsonProcessingException {
    String sessionId =
        SsoTestSetupUtils.signUpAndLogin(
            mockMailbox,
            objectMapper,
            "phone-number-verify-no-phone-number@gmail.com",
            "phone-number-verify-no-phone-number");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(10)))
        .patch(PHONE_NUMBER_VERIFY_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"phone_number\":\"Required\"}}}"));
  }

  @Test
  public void update_user__should_throw__when_invalid_content_type() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .patch(UserResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void update_user__should_throw__when_no_authentication() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, SsoSession.newIdentifier())
        .patch(UserResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void update_user__should_throw__when_no_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .patch(UserResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void update_user__should_throw__when_empty_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body("{}")
        .patch(UserResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void update_user__should_throw__when_invalid_body() throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body(JacksonMapper.get().writeValueAsString(Map.of("a", "b")))
        .patch(UserResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"a\":\"Unexpected field\"}}}"));
  }

  @Test
  public void update_user__should_work__when_valid_body() throws JsonProcessingException {
    String sessionId = loginWithInsightAdminFromAuthApi();
    DataResponse<UserDTO> updateUserDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(JacksonMapper.get().writeValueAsString(Map.of("phone_number", "+38651222333")))
            .patch(UserResource.PATH)
            .as(new TypeRef<>() {});

    assertEquals("+38651222333", updateUserDataResponse.getData().getPhoneNumber());
    assertFalse(updateUserDataResponse.getData().isPhoneNumberVerified());

    // Should also update the sessions
    DataResponse<UserDTO> getSessionDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(String.join("/", SsoResource.PATH, "me"))
            .as(new TypeRef<>() {});

    assertEquals(updateUserDataResponse.getData(), getSessionDataResponse.getData());

    // Send code for phone number verification
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(PHONE_NUMBER_VERIFY_SEND_CODE_PATH)
        .then()
        .statusCode(200)
        .body(SameJSON.sameJson("{\"data\":{\"validitySeconds\":60}}"));

    int verificationCode =
        AuthApiSetupUtils.getLastSmsMessageVerificationCode(
            mockSmsbox, updateUserDataResponse.getData().getPhoneNumber());

    // Verify phone number
    updateUserDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(
                JacksonMapper.get()
                    .writeValueAsString(new TfaChallengeCompleteDTO(verificationCode)))
            .patch(PHONE_NUMBER_VERIFY_PATH)
            .as(new TypeRef<>() {});

    assertTrue(updateUserDataResponse.getData().isPhoneNumberVerified());

    // Should also update the sessions
    getSessionDataResponse =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(String.join("/", SsoResource.PATH, "me"))
            .as(new TypeRef<>() {});
    assertEquals(updateUserDataResponse.getData(), getSessionDataResponse.getData());
  }
}
