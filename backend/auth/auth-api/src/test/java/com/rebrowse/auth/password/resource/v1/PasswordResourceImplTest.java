package com.rebrowse.auth.password.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.NotFoundException;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.core.MailingConstants;
import com.rebrowse.auth.password.model.dto.PasswordChangeRequestDTO;
import com.rebrowse.auth.password.model.dto.PasswordForgotRequestDTO;
import com.rebrowse.auth.password.model.dto.PasswordResetRequestDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.model.organization.PasswordPolicy;
import com.rebrowse.model.organization.PasswordPolicyCreateParams;
import com.rebrowse.model.user.User;
import com.rebrowse.shared.SharedConstants;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import io.quarkus.mailer.Mail;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class PasswordResourceImplTest extends AbstractAuthApiQuarkusTest {

  @TestHTTPResource(PasswordResource.PATH + "/forgot")
  protected URL passwordForgotEndpoint;

  @TestHTTPResource(PasswordResource.PATH + "/change")
  protected URL passwordChangeEndpoint;

  private String passwordResetEndpoint(String token) {
    return PasswordResource.PATH + "/reset/" + token;
  }

  private String passwordResetExistsEndpoint(String token) {
    return passwordResetEndpoint(token) + "/exists";
  }

  @Test
  public void password_reset_exists__should_be_false__when_random_token() {
    given()
        .when()
        .get(passwordResetExistsEndpoint(UUID.randomUUID().toString()))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void password_forgot__should_fail__when_invalid_content_type() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(passwordForgotEndpoint)
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
        .post(passwordForgotEndpoint)
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
        .post(passwordForgotEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void password_forgot__should_fail__when_empty_email() throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(new PasswordForgotRequestDTO("", null));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(passwordForgotEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void password_forgot__should_fail__when_invalid_email() throws JsonProcessingException {
    String payload =
        objectMapper.writeValueAsString(
            new PasswordForgotRequestDTO("notEmail", GlobalTestData.LOCALHOST_REDIRECT_URL));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(passwordForgotEndpoint)
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
        objectMapper.writeValueAsString(
            new PasswordForgotRequestDTO(
                "missing@test.com", GlobalTestData.LOCALHOST_REDIRECT_URL));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(passwordForgotEndpoint)
        .then()
        .statusCode(204);
  }

  @Test
  public void password_forgot__should_send_email__when_existing_user()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String email = authorizationFlows().retrieveUserData(sessionId).getUser().getEmail();

    passwordFlows().forgot(email);
    // can trigger the forgot flow multiple times!!
    passwordFlows().forgot(email);
  }

  @Test
  public void reset__should_fail__when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(passwordResetEndpoint(UUID.randomUUID().toString()))
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
        .post(passwordResetEndpoint(UUID.randomUUID().toString()))
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
        .post(passwordResetEndpoint(UUID.randomUUID().toString()))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\"}}}"));
  }

  @Test
  public void password_reset__should_fail__when_invalid_payload() throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(new PasswordResetRequestDTO(""));
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(passwordResetEndpoint(UUID.randomUUID().toString()))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\"}}}"));
  }

  @Test
  public void password_reset_should_fail_when_missing_payload() throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(new PasswordResetRequestDTO("passLongEnough"));
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(passwordResetEndpoint(UUID.randomUUID().toString()))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Password reset request not found\"}}"));
  }

  @Test
  public void password_reset_flow__should_require_verification__if_mfa_setup()
      throws IOException, NotFoundException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    User user = authorizationFlows().retrieveUserData(sessionId).getUser();
    String totpSecret = mfaSetupFlows().setupTotpSuccess(user, sessionId).getRight();

    // init flow
    passwordFlows().forgot(user.getEmail());

    List<Mail> sent = mailbox.getMessagesSentTo(user.getEmail());
    assertEquals(2, sent.size());
    Mail actual = sent.get(1);
    Assertions.assertEquals(MailingConstants.FROM_SUPPORT, actual.getFrom());

    Document doc = Jsoup.parse(actual.getHtml());
    Elements link = doc.select("a");
    String passwordForgotLink = link.attr("href");

    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(passwordForgotLink);
    if (!tokenMatcher.matches()) {
      throw new RuntimeException();
    }
    String token = tokenMatcher.group(1);

    // password reset should go into authentication flow
    String challengeId =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                objectMapper.writeValueAsString(
                    new PasswordResetRequestDTO("superDuperNewFancyPassword")))
            .post(passwordResetEndpoint(token))
            .then()
            .statusCode(200)
            .extract()
            .detailedCookie(AuthorizationMfaChallengeSession.COOKIE_NAME)
            .getValue();

    mfaAuthorizationFlows().completeTotpChallenge(totpSecret, challengeId);
  }

  @Test
  public void password_reset_flow__should_succeed__after_sign_up() throws JsonProcessingException {
    String oldPassword = UUID.randomUUID().toString();
    String email = AuthApiTestUtils.randomBusinessEmail();
    signUpFlows().signUpAndLogin(email, oldPassword);
    passwordFlows().forgot(email);

    // login with "oldPassword" should succeed
    authorizationFlows().login(email, oldPassword);

    List<Mail> sent = mailbox.getMessagesSentTo(email);
    assertEquals(2, sent.size());
    Mail actual = sent.get(1);
    assertEquals(MailingConstants.FROM_SUPPORT, actual.getFrom());

    Document doc = Jsoup.parse(actual.getHtml());
    Elements link = doc.select("a");
    String passwordForgotLink = link.attr("href");

    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(passwordForgotLink);
    if (!tokenMatcher.matches()) {
      throw new RuntimeException();
    }
    String token = tokenMatcher.group(1);

    assertEquals(
        passwordForgotLink,
        String.format(
            "https://www.%s/password-reset?token=%s",
            SharedConstants.REBROWSE_STAGING_DOMAIN, token));

    // reset request should exist
    given()
        .when()
        .get(passwordResetExistsEndpoint(token))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    String newPassword = UUID.randomUUID().toString();
    String resetPasswordPayload =
        objectMapper.writeValueAsString(new PasswordResetRequestDTO(newPassword));

    // successful reset should login the user
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(resetPasswordPayload)
        .post(passwordResetEndpoint(token))
        .then()
        .statusCode(200)
        .cookie(SsoSession.COOKIE_NAME)
        .body(
            sameJson(
                "{\"data\":{\"location\":\"http://localhost:3000/test\",\"action\":\"SUCCESS\"}}"));

    // login with "oldPassword" should fail
    pwdAuthorizationFlows().completePwdChallengeInvalidCredentials(email, oldPassword);

    // login with "newPassword" should succeed
    authorizationFlows().login(email, newPassword);

    // trying to do reset with same token again should fail
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(resetPasswordPayload)
        .post(passwordResetEndpoint(token))
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
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void password_change__should_fail__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.POST, passwordChangeEndpoint.toString(), ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.POST, passwordChangeEndpoint.toString(), ContentType.JSON);
  }

  @Test
  public void password_change__should_fail__when_missing_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void password_change__should_fail__when_empty_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"confirmNewPassword\":\"Required\",\"newPassword\":\"Required\",\"currentPassword\":\"Required\"}}}"));
  }

  @Test
  public void password_change__should_fail__when_invalid_body() throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new PasswordChangeRequestDTO("", "", "")))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"confirmNewPassword\":\"Required\",\"newPassword\":\"Required\",\"currentPassword\":\"Required\"}}}"));
  }

  @Test
  public void password_change__should_fail__when_password_miss_match()
      throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO("password12345", "password123", "password1234");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(passwordChangeRequestDTO))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Passwords must match!\"}}"));
  }

  @Test
  public void password_change__should_fail__when_password_duplicate_early_check()
      throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO(
            "superDuperPassword123", "superDuperPassword123", "superDuperPassword123");

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(passwordChangeRequestDTO))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"newPassword\":\"New password cannot be the same as the previous one!\"}}}"));
  }

  @Test
  public void password_change__should_fail__when_duplicate_db_check()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String oldPassword = UUID.randomUUID().toString();
    String newPassword = UUID.randomUUID().toString();
    String sessionId = signUpFlows().signUpAndLogin(email, oldPassword);

    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO(UUID.randomUUID().toString(), newPassword, newPassword);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(passwordChangeRequestDTO))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Current password miss match\"}}"));
  }

  @Test
  public void password_change__should_work__when_different_password()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String oldPassword = UUID.randomUUID().toString();
    String newPassword = UUID.randomUUID().toString();
    String sessionId = signUpFlows().signUpAndLogin(email, oldPassword);
    PasswordChangeRequestDTO passwordChangeRequestDTO =
        new PasswordChangeRequestDTO(oldPassword, newPassword, newPassword);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(passwordChangeRequestDTO))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(204);

    String challengeId = authorizationFlows().chooseAccount(email);
    pwdAuthorizationFlows().completePwdChallengeInvalidCredentials(email, oldPassword, challengeId);

    String newSessionId = authorizationFlows().login(email, newPassword);
    assertNotEquals(newSessionId, sessionId);
  }

  @Test
  public void change_password__should_fail__when_organization_password_policy_violated()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = AuthApiTestUtils.randomBusinessEmail();
    String sessionId = signUpFlows().signUpAndLogin(email, password);

    PasswordPolicy.create(
            PasswordPolicyCreateParams.builder()
                .minCharacters((short) 15)
                .preventPasswordReuse(true)
                .requireNonAlphanumericCharacter(true)
                .build(),
            sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(
            objectMapper.writeValueAsString(
                new PasswordChangeRequestDTO(password, password, password)))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"newPassword\":\"New password cannot be the same as the previous one!\"}}}"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(
            objectMapper.writeValueAsString(
                new PasswordChangeRequestDTO(password, "password123", "password123")))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"newPassword\":\"Password should contain at least 15 characters\"}}}"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(
            objectMapper.writeValueAsString(
                new PasswordChangeRequestDTO(password, "password123456789", "password123456789")))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"newPassword\":\"Password should contain at least one non-alphanumeric character\"}}}"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(
            objectMapper.writeValueAsString(
                new PasswordChangeRequestDTO(password, "password123456789!", "password123456789!")))
        .post(passwordChangeEndpoint)
        .then()
        .statusCode(204);
  }
}
