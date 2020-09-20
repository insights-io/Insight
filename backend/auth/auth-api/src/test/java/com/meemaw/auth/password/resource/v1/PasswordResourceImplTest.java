package com.meemaw.auth.password.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.password.model.dto.PasswordChangeRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordForgotRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.session.resource.v1.SsoResource;
import com.meemaw.auth.sso.tfa.challenge.model.SsoChallenge;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.challenge.resource.v1.TfaChallengeResourceImpl;
import com.meemaw.auth.sso.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.sso.tfa.totp.impl.TotpUtils;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.utils.AuthApiSetupUtils;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.Mail;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class PasswordResourceImplTest extends AbstractAuthApiTest {

  public static final String PASSWORD_FORGOT_PATH =
      String.join("/", PasswordResource.PATH, "password_forgot");

  public static final String PASSWORD_CHANGE_PATH =
      String.join("/", PasswordResource.PATH, "password_change");

  public static final String PASSWORD_RESET_PATH_TEMPLATE =
      String.join("/", PasswordResource.PATH, "password_reset", "%s");

  public static final String PASSWORD_RESET_EXISTS_PATH_TEMPLATE =
      String.join("/", PasswordResource.PATH, "password_reset", "%s", "exists");

  @Inject UserDatasource userDatasource;
  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;

  @Test
  public void password_reset_exists__should_fail__when_random_token() {
    given()
        .when()
        .get(String.format(PASSWORD_RESET_EXISTS_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void password_forgot__should_fail__when_invalid_content_type() {
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
  public void password_forgot__should_fail__when_no_payload() {
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
  public void password_forgot__should_fail__when_empty_payload() {
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
  public void password_forgot__should_fail__when_empty_email() throws JsonProcessingException {
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
  public void password_forgot__should_fail__when_invalid_email() throws JsonProcessingException {
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
  public void password_forgot__should_succeed__when_missing_email_to_not_leak_users()
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
  public void password_forgot__should_send_email__when_existing_user()
      throws JsonProcessingException {
    String email = "test-forgot-password-flow@gmail.com";
    String password = "superHardPassword";
    authApi().signUpAndLogin(email, password);
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
            .header("referer", "https://www.insight.io")
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .post(PASSWORD_FORGOT_PATH);

    response.then().statusCode(201).body(sameJson("{\"data\":true}"));

    return response;
  }

  @Test
  public void reset__should_fail__when_invalid_contentType() {
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
  public void password_reset__should_fail__when_no_payload() {
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
  public void password_reset__should_fail__when_empty_payload() {
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
  public void password_reset__should_fail__when_invalid_payload() throws JsonProcessingException {
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
  public void password_reset_flow__should_require_verification__if_tfa_setup()
      throws JsonProcessingException, GeneralSecurityException {
    String email = "reset-password-flow-tfa@gmail.com";
    String password = "superHardPassword";
    String sessionId = authApi().signUpAndLogin(email, password);
    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    String secret = AuthApiSetupUtils.setupTotpTfa(userId, sessionId, tfaTotpSetupDatasource);

    // init flow
    PasswordResourceImplTest.passwordForgot(email, objectMapper);

    String newPassword = "superDuperNewFancyPassword";
    String resetPasswordPayload =
        objectMapper.writeValueAsString(new PasswordResetRequestDTO(newPassword));

    List<Mail> sent = mailbox.getMessagesSentTo(email);
    assertEquals(2, sent.size());
    Mail actual = sent.get(1);
    assertEquals("Insight Support <support@insight.com>", actual.getFrom());

    Document doc = Jsoup.parse(actual.getHtml());
    Elements link = doc.select("a");
    String passwordForgotLink = link.attr("href");

    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(passwordForgotLink);
    tokenMatcher.matches();
    String token = tokenMatcher.group(1);

    // password reset should go into authentication flow
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(resetPasswordPayload)
            .post(String.format(PASSWORD_RESET_PATH_TEMPLATE, token));

    response.then().statusCode(200).cookie(SsoChallenge.COOKIE_NAME);
    String challengeId = response.getDetailedCookie(SsoChallenge.COOKIE_NAME).getValue();

    // Complete tfa flow
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoChallenge.COOKIE_NAME, challengeId)
        .body(
            JacksonMapper.get()
                .writeValueAsString(
                    new TfaChallengeCompleteDTO(TotpUtils.generateCurrentNumber(secret))))
        .post(TfaChallengeResourceImpl.PATH + "/totp/complete")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"))
        .cookie(SsoChallenge.COOKIE_NAME, "")
        .cookie(SsoSession.COOKIE_NAME);
  }

  @Test
  public void password_reset_flow__should_succeed__after_sign_up() throws JsonProcessingException {
    String signUpEmail = "reset-password-flow@gmail.com";
    String oldPassword = "superHardPassword";
    authApi().signUpAndLogin(signUpEmail, oldPassword);
    PasswordResourceImplTest.passwordForgot(signUpEmail, objectMapper);

    // login with "oldPassword" should succeed
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signUpEmail)
        .param("password", oldPassword)
        .header("referer", "http://localhost:3000")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"));

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

    assertEquals(passwordForgotLink, "https://www.insight.io/password-reset?token=" + token);

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
        .statusCode(200)
        .cookie(SsoSession.COOKIE_NAME)
        .body(sameJson("{\"data\": true}"));

    // login with "oldPassword" should fail
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signUpEmail)
        .param("password", oldPassword)
        .header("referer", "http://localhost:3000")
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
        .header("referer", "http://localhost:3000")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
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

  @Test
  public void password_change__should_fail_when__invalid_content_type() {
    given()
        .when()
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void password_change__should_fail__when_no_auth() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void password_change__should_fail__when_random_session_id() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, "random")
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void password_change__should_fail__when_missing_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void password_change__should_fail__when_empty_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .body("{}")
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"confirmNewPassword\":\"Required\",\"newPassword\":\"Required\",\"currentPassword\":\"Required\"}}}"));
  }

  @Test
  public void password_change__should_fail__when_invalid_body() throws JsonProcessingException {
    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO("haba", "aba", "caba");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .body(JacksonMapper.get().writeValueAsString(passwordChangeRequestDTO))
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"confirmNewPassword\":\"Password must be at least 8 characters long\",\"newPassword\":\"Password must be at least 8 characters long\",\"currentPassword\":\"Password must be at least 8 characters long\"}}}"));
  }

  @Test
  public void password_change__should_fail__when_password_miss_match()
      throws JsonProcessingException {
    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO("password12345", "password123", "password1234");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .body(JacksonMapper.get().writeValueAsString(passwordChangeRequestDTO))
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Passwords must match!\"}}"));
  }

  @Test
  public void password_change__should_fail__when_password_duplicate_early_check()
      throws JsonProcessingException {
    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO(
            "superDuperPassword123", "superDuperPassword123", "superDuperPassword123");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .body(JacksonMapper.get().writeValueAsString(passwordChangeRequestDTO))
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"New password cannot be the same as the previous one!\"}}"));
  }

  @Test
  public void password_change__should_fail__when_duplicate_db_check()
      throws JsonProcessingException {
    String email = "password-change-duplicate-test@gmail.com";
    String oldPassword = "password123";
    String newPassword = "superDuperPassword123";
    String sessionId = authApi().signUpAndLogin(email, oldPassword);
    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO("password1234", newPassword, newPassword);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(passwordChangeRequestDTO))
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Current password miss match\"}}"));
  }

  @Test
  public void password_change__should_work__when_different_password()
      throws JsonProcessingException {
    String email = "password-change-test@gmail.com";
    String oldPassword = "password123";
    String newPassword = "superDuperPassword123";
    String sessionId = authApi().signUpAndLogin(email, oldPassword);
    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO(oldPassword, newPassword, newPassword);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(passwordChangeRequestDTO))
        .post(PASSWORD_CHANGE_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", email)
        .param("password", oldPassword)
        .header("referer", "http://localhost:3000")
        .post(SsoResource.PATH + "/login")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));

    String newSessionId = authApi().login(email, newPassword);
    assertNotEquals(newSessionId, sessionId);
  }
}
