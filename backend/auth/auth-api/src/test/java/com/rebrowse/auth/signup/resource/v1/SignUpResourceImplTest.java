package com.rebrowse.auth.signup.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static com.rebrowse.test.utils.EmailTestUtils.parseLink;
import static com.rebrowse.test.utils.RestAssuredUtils.dontFollowRedirects;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.auth.core.MailingConstants;
import com.rebrowse.auth.signup.model.dto.SignUpRequestDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.shared.SharedConstants;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.test.utils.EmailTestUtils;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import io.quarkus.mailer.Mail;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class SignUpResourceImplTest extends AbstractAuthApiQuarkusTest {

  private String signUpValidEndpoint(String token) {
    return String.join("/", SignUpResource.PATH, token, "valid");
  }

  @Test
  public void sign_up_valid__should_throw__when_random_id() {
    given()
        .when()
        .get(signUpValidEndpoint(UUID.randomUUID().toString()))
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
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\",\"password\":\"Required\",\"fullName\":\"Required\",\"company\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void sign_up__should_fail__when_invalid_payload() throws JsonProcessingException {
    SignUpRequestDTO request =
        new SignUpRequestDTO(
            RequestUtils.sneakyUrl(GlobalTestData.LOCALHOST_REDIRECT),
            "email",
            "short",
            "Marko Novak",
            SharedConstants.REBROWSE_ORGANIZATION_NAME,
            new PhoneNumberDTO(null, null));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(request))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\",\"phoneNumber.countryCode\":\"Required\",\"phoneNumber.digits\":\"Required\",\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void sign_up__should_redirect_back_to_redirect__when_valid_payload()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    SignUpRequestDTO request =
        new SignUpRequestDTO(
            RequestUtils.sneakyUrl(GlobalTestData.LOCALHOST_REDIRECT),
            email,
            "not_short_123",
            "Marko Novak",
            SharedConstants.REBROWSE_ORGANIZATION_NAME,
            null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(request))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    // completing sign up with with referrer should redirect back to it
    given()
        .when()
        .config(dontFollowRedirects())
        .get(parseLink(mailbox.getMessagesSentTo(email).get(0)))
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .cookie(SsoSession.COOKIE_NAME)
        .header("Location", GlobalTestData.LOCALHOST_REDIRECT);
  }

  @Test
  public void sign_up__should_succeeded() throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    SignUpRequestDTO request =
        new SignUpRequestDTO(
            RequestUtils.sneakyUrl(GlobalTestData.LOCALHOST_REDIRECT),
            email,
            "not_short_123",
            "Marko Novak",
            SharedConstants.REBROWSE_ORGANIZATION_NAME,
            null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(request))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    // trying to sign up with the same email address again should succeed but not send additional
    // email due to `auth.sign_up_request` conflict.
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(request))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    List<Mail> sent = mailbox.getMessagesSentTo(email);
    assertEquals(1, sent.size());

    Mail completeSignUpMail = sent.get(0);
    Assertions.assertEquals(MailingConstants.FROM_SUPPORT, completeSignUpMail.getFrom());
    String signUpCompleteLink = EmailTestUtils.parseLink(completeSignUpMail);
    String token = EmailTestUtils.parseTokenFromSignUpCompleteLink(signUpCompleteLink);

    // verify that the SignUpRequest exists & is valid
    given()
        .when()
        .get(signUpValidEndpoint(token))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    // complete the sign up
    given()
        .config(RestAssuredUtils.dontFollowRedirects())
        .when()
        .get(signUpCompleteLink)
        .then()
        .statusCode(302)
        .cookie(SsoSession.COOKIE_NAME)
        .header(HttpHeaders.LOCATION, GlobalTestData.LOCALHOST_REDIRECT);

    // verify that the SignUpRequest is not valid anymore
    given()
        .when()
        .get(signUpValidEndpoint(token))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));

    // trying to sign up with the same email address again should succeed but not send additional
    // email due to `auth.user` conflict.
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(request))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    assertEquals(1, sent.size());
  }
}
