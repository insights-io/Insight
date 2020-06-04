package com.meemaw.auth.signup.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SignUpResourceImplTest {

  @TestHTTPResource(SignUpResource.PATH)
  URL signUpResourceBasePath;

  @Inject MockMailbox mailbox;

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  @Test
  public void sign_up_not_valid_on_random_id() {
    given()
        .when()
        .get(String.join("/", SignUpResource.PATH, UUID.randomUUID().toString(), "valid"))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void sign_up_complete_fails_on_random_id() {
    given()
        .when()
        .get(String.join("/", SignUpResource.PATH, UUID.randomUUID().toString(), "complete"))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void signUp_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(SignUpResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void signUp_should_fail_when_no_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(SignUpResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void signUp_should_fail_when_empty_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .post(SignUpResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\",\"fullName\":\"Required\",\"company\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void signUp_should_fail_when_empty_invalid_payload() throws JsonProcessingException {
    SignUpRequestDTO signUpRequestDTO =
        new SignUpRequestDTO("email", "short", "Marko Novak", "Insight", null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\",\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void sign_up_client_redirect_full_flow_succeed_on_valid_payload()
      throws JsonProcessingException {
    String referer = "http://localhost:3000";
    String signUpEmail = "marko.skace@insight.io";
    SignUpRequestDTO signUpRequestDTO =
        new SignUpRequestDTO(signUpEmail, "not_short_123", "Marko Novak", "Insight", null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header("referer", referer)
        .body(JacksonMapper.get().writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    // completing sign up with with referer should redirect back to it
    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .get(SsoTestSetupUtils.parseLink(mailbox.getMessagesSentTo(signUpEmail).get(0)))
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .cookie(SsoSession.COOKIE_NAME)
        .header("Location", String.join("/", referer, "signup-completed-callback"));
  }

  @Test
  public void signUp_full_flow_succeed_on_valid_payload() throws JsonProcessingException {
    String signUpEmail = "marko.novak@insight.io";
    SignUpRequestDTO signUpRequestDTO =
        new SignUpRequestDTO(signUpEmail, "not_short_123", "Marko Novak", "Insight", null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    // trying to sign up with the same email address again should succeed but not send additional
    // email due to `auth.sign_up_request` conflict.
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    List<Mail> sent = mailbox.getMessagesSentTo(signUpEmail);
    assertEquals(1, sent.size());

    Mail completeSignUpMail = sent.get(0);
    assertEquals("Insight Support <support@insight.com>", completeSignUpMail.getFrom());
    String token = SsoTestSetupUtils.parseConfirmationToken(completeSignUpMail);

    assertThat(
        completeSignUpMail.getHtml(),
        containsString(String.join("/", signUpResourceBasePath.getPath(), token, "complete")));

    // verify that the SignUpRequest exists & is valid
    given()
        .when()
        .get(String.join("/", SignUpResource.PATH, token, "valid"))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    // complete the sign up
    given()
        .when()
        .get(String.join("/", SignUpResource.PATH, token, "complete"))
        .then()
        .statusCode(204)
        .cookie(SsoSession.COOKIE_NAME);

    // verify that the SignUpRequest does not exist anymore
    given()
        .when()
        .get(String.join("/", SignUpResource.PATH, token, "valid"))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));

    // trying to sign up with the same email address again should succeed but not send additional
    // email due to `auth.user` conflict.
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    assertEquals(1, sent.size());
  }
}
