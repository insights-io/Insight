package com.meemaw.auth.signup.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.EmailTestUtils.parseLink;
import static com.meemaw.test.setup.RestAssuredUtils.dontFollowRedirects;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.shared.SharedConstants;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.setup.EmailTestUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.Mail;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.http.HttpHeaders;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SignUpResourceImplTest extends AbstractAuthApiTest {

  @TestHTTPResource(SignUpResource.PATH)
  URL signUpResourceBasePath;

  @Test
  public void sign_up_valid__should_throw__when_random_id() {
    given()
        .when()
        .get(String.join("/", SignUpResource.PATH, UUID.randomUUID().toString(), "valid"))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void sign_up_complete__should_throw__when_random_id() {
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
  public void sign_up__should_fail__when_invalid_content_type() {
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
  public void sign_up__should_fail__when_no_payload() {
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
  public void sign_up__should_fail__when_empty_payload() {
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
  public void sign_up__should_fail__when_empty_invalid_payload() throws JsonProcessingException {
    SignUpRequestDTO signUpRequestDTO =
        new SignUpRequestDTO(
            "email",
            "short",
            "Marko Novak",
            SharedConstants.ORGANIZATION_NAME,
            new PhoneNumberDTO(null, null));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\",\"phoneNumber.countryCode\":\"Required\",\"phoneNumber.digits\":\"Required\",\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void sign_up__should_redirect_back_to_referer__when_valid_payload()
      throws JsonProcessingException {
    String referrer = "http://localhost:3000";
    String signUpEmail = String.format("%s@gmail.com", UUID.randomUUID());
    SignUpRequestDTO signUpRequestDTO =
        new SignUpRequestDTO(
            signUpEmail, "not_short_123", "Marko Novak", SharedConstants.ORGANIZATION_NAME, null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.REFERER.toString(), referrer)
        .body(objectMapper.writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    // completing sign up with with referrer should redirect back to it
    given()
        .when()
        .config(dontFollowRedirects())
        .get(parseLink(mailbox.getMessagesSentTo(signUpEmail).get(0)))
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .cookie(SsoSession.COOKIE_NAME)
        .header("Location", String.join("/", referrer, "signup-completed-callback"));
  }

  @Test
  public void sign_up__should_succeeded() throws JsonProcessingException {
    String signUpEmail = String.format("%s@gmail.com", UUID.randomUUID());
    SignUpRequestDTO signUpRequestDTO =
        new SignUpRequestDTO(
            signUpEmail, "not_short_123", "Marko Novak", SharedConstants.ORGANIZATION_NAME, null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    // trying to sign up with the same email address again should succeed but not send additional
    // email due to `auth.sign_up_request` conflict.
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    List<Mail> sent = mailbox.getMessagesSentTo(signUpEmail);
    assertEquals(1, sent.size());

    Mail completeSignUpMail = sent.get(0);
    assertEquals(MailingConstants.FROM_SUPPORT, completeSignUpMail.getFrom());
    String token = EmailTestUtils.parseConfirmationToken(completeSignUpMail);

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
        .statusCode(200)
        .cookie(SsoSession.COOKIE_NAME)
        .body(sameJson("{\"data\":true}"));

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
        .body(objectMapper.writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    assertEquals(1, sent.size());
  }
}
