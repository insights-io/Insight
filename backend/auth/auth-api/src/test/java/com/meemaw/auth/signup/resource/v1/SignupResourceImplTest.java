package com.meemaw.auth.signup.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.auth.password.resource.v1.PasswordResource;
import com.meemaw.auth.password.resource.v1.PasswordResourceImplTest;
import com.meemaw.auth.signup.model.dto.SignupRequestCompleteDTO;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.resource.v1.SsoResourceImplTest;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class SignupResourceImplTest {

  @Inject MockMailbox mailbox;

  @Inject ObjectMapper objectMapper;

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  @Test
  public void signup_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(SignupResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void signup_should_fail_when_no_email() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .post(SignupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg0\":\"Email is required\"}}}"));
  }

  @Test
  public void signup_should_fail_when_invalid_email() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", "random")
        .post(SignupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg0\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void signupExists_should_fail_when_no_payload() {
    given()
        .when()
        .get(SignupResource.PATH + "/exists")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg2\":\"token is required\",\"arg1\":\"org is required\",\"arg0\":\"email is required\"}}}"));
  }

  @Test
  public void signupExists_should_fail_when_invalid_params() {
    given()
        .when()
        .queryParam("email", "notEmail")
        .queryParam("org", "")
        .queryParam("token", "4f113105-94d9-4470-8621-0e633fa46977")
        .get(SignupResource.PATH + "/exists")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg1\":\"org is required\",\"arg0\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void signupExists_should_return_false_when_missing_params() {
    given()
        .when()
        .queryParam("email", "test@gmail.com")
        .queryParam("org", "random")
        .queryParam("token", "4f113105-94d9-4470-8621-0e633fa4697")
        .get(SignupResource.PATH + "/exists")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void signupComplete_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(SignupResource.PATH + "/complete")
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void signupComplete_should_fail_when_no_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(SignupResource.PATH + "/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"arg0\":\"Payload is required\"}}}"));
  }

  @Test
  public void signupComplete_should_fail_when_empty_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(SignupResource.PATH + "/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\",\"org\":\"Required\",\"email\":\"Required\",\"token\":\"Required\"}}}"));
  }

  @Test
  public void signupComplete_should_fail_when_invalid_payload_2()
      throws URISyntaxException, IOException {
    String payload =
        Files.readString(
            Path.of(getClass().getResource("/signup/complete/invalidSecond.json").toURI()));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(SignupResource.PATH + "/complete")
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\",\"org\":\"Required\",\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void signupComplete_should_fail_when_missing_payload()
      throws URISyntaxException, IOException {
    String payload =
        Files.readString(Path.of(getClass().getResource("/signup/complete/missing.json").toURI()));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(SignupResource.PATH + "/complete")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                " {\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Signup request does not exist.\"}}"));
  }

  @Test
  public void signup_flow_should_succeed_on_valid_email() {
    String signupEmail = "test@gmail.com";
    String signupPassword = "superDuperPassword";

    signup(mailbox, objectMapper, signupEmail, signupPassword);

    // should be able to login with the newly created account
    SsoResourceImplTest.login(signupEmail, signupPassword);
  }

  @Test
  public void signup_flow_should_be_completable_with_password_reset()
      throws JsonProcessingException {
    String signupEmail = "signup-with-password-reset@gmail.com";

    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signupEmail)
        .post(SignupResource.PATH)
        .then()
        .statusCode(200);

    PasswordResourceImplTest.passwordForgot(signupEmail, objectMapper);

    List<Mail> sent = mailbox.getMessagesSentTo(signupEmail);
    assertEquals(2, sent.size());
    Mail passwordResetMail = sent.get(1);
    assertEquals("Insight Support <support@insight.com>", passwordResetMail.getFrom());
    Document doc = Jsoup.parse(passwordResetMail.getHtml());
    Elements link = doc.select("a");
    String passwordForgotLink = link.attr("href");
    Matcher orgMatcher = Pattern.compile("^.*orgId=(.*)&.*$").matcher(passwordForgotLink);
    orgMatcher.matches();
    String passwordResetOrgId = orgMatcher.group(1);
    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(passwordForgotLink);
    tokenMatcher.matches();
    String passwordResetToken = tokenMatcher.group(1);
    Matcher emailMatcher = Pattern.compile("^.*email=(.*\\.com).*$").matcher(passwordForgotLink);
    emailMatcher.matches();
    String passwordResetEmail = emailMatcher.group(1);

    String password = "superDuperPassword";
    String resetPasswordPayload =
        objectMapper.writeValueAsString(
            new PasswordResetRequestDTO(
                passwordResetEmail,
                passwordResetOrgId,
                UUID.fromString(passwordResetToken),
                password));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(resetPasswordPayload)
        .post(PasswordResource.PATH + "/reset")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME);

    // should be able to login with the password
    SsoResourceImplTest.login(signupEmail, password);

    Mail signupCompleteEmail = sent.get(0);
    assertEquals("Insight Support <support@insight.com>", signupCompleteEmail.getFrom());
    doc = Jsoup.parse(signupCompleteEmail.getHtml());
    link = doc.select("a");
    String signupCompleteUrl = link.attr("href");
    orgMatcher = Pattern.compile("^.*orgId=(.*)&.*$").matcher(signupCompleteUrl);
    orgMatcher.matches();
    String orgId = orgMatcher.group(1);
    tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(signupCompleteUrl);
    tokenMatcher.matches();
    String token = tokenMatcher.group(1);

    String completeSignupPayload =
        objectMapper.writeValueAsString(
            new SignupRequestCompleteDTO(
                signupEmail, orgId, UUID.fromString(token), "somePasswordHere"));

    // trying to complete the signup should fail at this point
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(completeSignupPayload)
        .post(SignupResource.PATH + "/complete")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                " {\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Signup request does not exist.\"}}"));
  }

  public static void signup(
      MockMailbox mailbox, ObjectMapper objectMapper, String signupEmail, String signupPassword) {
    given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", signupEmail)
        .post(SignupResource.PATH)
        .then()
        .statusCode(200);

    List<Mail> sent = mailbox.getMessagesSentTo(signupEmail);
    assertEquals(1, sent.size());
    Mail actual = sent.get(0);
    assertEquals("Insight Support <support@insight.com>", actual.getFrom());

    Document doc = Jsoup.parse(actual.getHtml());
    Elements link = doc.select("a");
    String signupCompleteUrl = link.attr("href");

    Matcher orgMatcher = Pattern.compile("^.*orgId=(.*)&.*$").matcher(signupCompleteUrl);
    orgMatcher.matches();
    String orgId = orgMatcher.group(1);

    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(signupCompleteUrl);
    tokenMatcher.matches();
    String token = tokenMatcher.group(1);

    // verify that the SignupRequest still exists & is valid
    given()
        .when()
        .formParam("email", signupEmail)
        .queryParam("org", orgId)
        .queryParam("token", token)
        .get(SignupResource.PATH + "/exists")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    SignupRequestCompleteDTO signupCompleteRequest =
        new SignupRequestCompleteDTO(signupEmail, orgId, UUID.fromString(token), signupPassword);

    String body;
    try {
      body = objectMapper.writeValueAsString(signupCompleteRequest);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // complete the signup
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .post(SignupResource.PATH + "/complete")
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME);
  }
}
