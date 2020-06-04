package com.meemaw.auth.password.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.password.model.dto.PasswordForgotRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class PasswordResourceImplTest {

  public static final String PASSWORD_FORGOT_PATH =
      String.join("/", PasswordResource.PATH, "password_forgot");

  public static final String PASSWORD_RESET_PATH_TEMPLATE =
      String.join("/", PasswordResource.PATH, "password_reset", "%s");

  public static final String PASSWORD_RESET_EXISTS_PATH_TEMPLATE =
      String.join("/", PasswordResource.PATH, "password_reset", "%s", "exists");

  @Inject MockMailbox mailbox;

  @Inject ObjectMapper objectMapper;

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  @Test
  public void password_reset_exists_should_fail_when_random_token() {

    given()
        .when()
        .get(String.format(PASSWORD_RESET_EXISTS_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void password_forgot_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(PASSWORD_FORGOT_PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void password_forgot_should_fail_when_no_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(PASSWORD_FORGOT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void passsword_forgot_should_fail_when_empty_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(PASSWORD_FORGOT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"Required\"}}}"));
  }

  @Test
  public void password_forgot_should_fail_when_empty_email() throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(new PasswordForgotRequestDTO(""));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(PASSWORD_FORGOT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"Required\"}}}"));
  }

  @Test
  public void password_forgot_should_fail_on_invalid_email() throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(new PasswordForgotRequestDTO("notEmail"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(PASSWORD_FORGOT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void password_forgot_should_succeed_on_missing_email_to_not_leak_users()
      throws JsonProcessingException {
    String payload =
        objectMapper.writeValueAsString(new PasswordForgotRequestDTO("missing@test.com"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(PASSWORD_FORGOT_PATH)
        .then()
        .statusCode(201)
        .body(sameJson("{\"data\":true}"));
  }

  @Test
  public void password_forgot_should_send_email_on_existing_user() throws JsonProcessingException {
    String email = "test-forgot-password-flow@gmail.com";
    String password = "superHardPassword";
    SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);
    PasswordResourceImplTest.passwordForgot(email, objectMapper);
    // can trigger the forgot flow multiple times!!
    PasswordResourceImplTest.passwordForgot(email, objectMapper);
  }

  /**
   * Initializes a password forgot request flow and throws is it is not successful.
   *
   * @param email address
   * @param objectMapper object mapper
   * @return password forgot request response
   * @throws JsonProcessingException if failed to serialize password forgot request
   */
  public static Response passwordForgot(String email, ObjectMapper objectMapper)
      throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(new PasswordForgotRequestDTO(email));

    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .post(PASSWORD_FORGOT_PATH);

    response.then().statusCode(201).body(sameJson("{\"data\":true}"));

    return response;
  }

  @Test
  public void reset_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(String.format(PASSWORD_RESET_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void password_reset_should_fail_when_no_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(String.format(PASSWORD_RESET_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void password_reset_should_fail_when_empty_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(String.format(PASSWORD_RESET_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\"}}}"));
  }

  @Test
  public void password_reset_should_fail_when_invalid_payload() throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(new PasswordResetRequestDTO("pass"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(String.format(PASSWORD_RESET_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\"}}}"));
  }

  @Test
  public void password_reset_should_fail_when_missing_payload() throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(new PasswordResetRequestDTO("passLongEnough"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(String.format(PASSWORD_RESET_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Password reset request not found\"}}"));
  }

  @Test
  public void password_reset_flow_should_succeed_after_sign_up() throws JsonProcessingException {
    String signUpEmail = "reset-password-flow@gmail.com";
    String oldPassword = "superHardPassword";
    SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, signUpEmail, oldPassword);
    PasswordResourceImplTest.passwordForgot(signUpEmail, objectMapper);

    // login with "oldPassword" should succeed
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signUpEmail)
        .param("password", oldPassword)
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(204);

    List<Mail> sent = mailbox.getMessagesSentTo(signUpEmail);
    assertEquals(2, sent.size());
    Mail actual = sent.get(1);
    assertEquals("Insight Support <support@insight.com>", actual.getFrom());

    Document doc = Jsoup.parse(actual.getHtml());
    Elements link = doc.select("a");
    String passwordForgotLink = link.attr("href");

    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(passwordForgotLink);
    tokenMatcher.matches();
    String token = tokenMatcher.group(1);

    assertEquals(passwordForgotLink, "http://localhost:8081/password-reset?token=" + token);

    // reset request should exist
    given()
        .when()
        .get(String.format(PASSWORD_RESET_EXISTS_PATH_TEMPLATE, token))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    String newPassword = "superDuperNewFancyPassword";
    String resetPasswordPayload =
        objectMapper.writeValueAsString(new PasswordResetRequestDTO(newPassword));

    // successful reset should login the user
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(resetPasswordPayload)
        .post(String.format(PASSWORD_RESET_PATH_TEMPLATE, token))
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME);

    // login with "oldPassword" should fail
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signUpEmail)
        .param("password", oldPassword)
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));

    // login with "newPassword" should succeed
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signUpEmail)
        .param("password", newPassword)
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME);

    // trying to do reset with same token again should fail
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(resetPasswordPayload)
        .post(String.format(PASSWORD_RESET_PATH_TEMPLATE, token))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Password reset request not found\"}}"));
  }
}
