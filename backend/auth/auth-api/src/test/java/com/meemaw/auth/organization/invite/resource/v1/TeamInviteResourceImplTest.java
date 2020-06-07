package com.meemaw.auth.organization.invite.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.organization.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.organization.invite.model.dto.InviteCreateDTO;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
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
public class TeamInviteResourceImplTest {

  @Inject MockMailbox mailbox;

  @Inject ObjectMapper objectMapper;

  private static String sessionId;

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  /**
   * Signs up and logins with a test user if necessary.
   *
   * @return session id of the logged in test user
   */
  public String getSessionId() throws JsonProcessingException {
    if (sessionId == null) {
      String email = "org_invite_test@gmail.com";
      String password = "org_invite_test_password";
      sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);
    }

    return sessionId;
  }

  @Test
  public void invite_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void invite_should_fail_when_not_authenticated() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void invite_should_fail_when_no_payload() throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void invite_should_fail_when_empty_payload() throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .body("{}")
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"role\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void invite_should_fail_when_invalid_role() throws URISyntaxException, IOException {
    String payload =
        Files.readString(Path.of(getClass().getResource("/org/invite/invalidRole.json").toURI()));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .body(payload)
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(422)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"Unprocessable Entity\",\"errors\":{\"role\":\"not one of the values accepted for Enum class: [STANDARD, ADMIN]\"}}}"));
  }

  @Test
  public void invite_should_fail_when_invalid_email() throws IOException {
    String payload =
        objectMapper.writeValueAsString(new InviteCreateDTO("notEmail", UserRole.ADMIN));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .body(payload)
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void invite_flow_should_succeed_on_valid_payload() throws IOException {
    String payload =
        objectMapper.writeValueAsString(
            new InviteCreateDTO("test-team-invitation@gmail.com", UserRole.ADMIN));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .body(payload)
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(201);

    // creating same invite twice should fail with 409 Conflict
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .body(payload)
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(409)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":409,\"reason\":\"Conflict\",\"message\":\"User has already been invited\"}}"));

    // accept the invite
    List<Mail> sent = mailbox.getMessagesSentTo("test-team-invitation@gmail.com");
    assertEquals(1, sent.size());
    Mail actual = sent.get(0);
    assertEquals("Insight Support <support@insight.com>", actual.getFrom());

    Document doc = Jsoup.parse(actual.getHtml());
    Elements link = doc.select("a");
    String acceptInviteUrl = link.attr("href");

    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(acceptInviteUrl);
    tokenMatcher.matches();
    String token = tokenMatcher.group(1);

    String inviteAcceptPayload =
        objectMapper.writeValueAsString(
            new InviteAcceptDTO("Marko Novak", "superDuperPassword123"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(inviteAcceptPayload)
        .post(String.join("/", TeamInviteResource.PATH, token, "accept"))
        .then()
        .statusCode(201)
        .body(sameJson("{\"data\":true}"));
  }

  @Test
  public void list_invites_should_fail_when_not_authenticated() {
    given()
        .when()
        .get(TeamInviteResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void list_invites_should_return_collection() throws IOException {
    String sessionId =
        SsoTestSetupUtils.signUpAndLogin(
            mailbox, objectMapper, "list-invites-fetcher@gmail.com", "list-invites-fetcher");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TeamInviteResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"))
        .body("data.size()", is(0));

    String payload =
        objectMapper.writeValueAsString(
            new InviteCreateDTO("list-invites-test@gmail.com", UserRole.STANDARD));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(201);

    Response response =
        given().when().cookie(SsoSession.COOKIE_NAME, sessionId).get(TeamInviteResource.PATH);
    response.then().statusCode(200).body("data.size()", is(1));

    UUID token = UUID.fromString(response.body().path("data[0].token"));

    // delete the created invite
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .pathParam("token", token)
        .delete(TeamInviteResource.PATH + "/{token}")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    // should return 0 invites now
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TeamInviteResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"))
        .body("data.size()", is(0));
  }

  @Test
  public void delete_invite_should_fail_when_no_token_param() {
    given()
        .when()
        .delete(TeamInviteResource.PATH)
        .then()
        .statusCode(405)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":405,\"reason\":\"Method Not Allowed\",\"message\":\"Method Not Allowed\"}}"));
  }

  @Test
  public void delete_invite_should_fail_when_not_authenticated() {
    given()
        .when()
        .pathParam("token", UUID.randomUUID())
        .delete(TeamInviteResource.PATH + "/{token}")
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void delete_invite_should_fail_when_invalid_token_param() throws JsonProcessingException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .pathParam("token", "randomToken")
        .delete(TeamInviteResource.PATH + "/{token}")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @Test
  public void send_invite_should_fail_when_invalid_contentType() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(String.join("/", TeamInviteResource.PATH, UUID.randomUUID().toString(), "send"))
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void send_invite_should_fail_when_not_authenticated() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(String.join("/", TeamInviteResource.PATH, UUID.randomUUID().toString(), "send"))
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void send_invite_flow_should_succeed_on_existing_invite() throws JsonProcessingException {
    String invitedUserEmail = "send-invite-flow@gmail.com";
    String invitePayload =
        objectMapper.writeValueAsString(new InviteCreateDTO(invitedUserEmail, UserRole.ADMIN));

    // Invite the user
    given()
        .header("referer", "https://www.insight.io")
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .body(invitePayload)
        .post(TeamInviteResource.PATH)
        .then()
        .statusCode(201);

    List<Mail> sent = mailbox.getMessagesSentTo(invitedUserEmail);
    assertEquals(1, sent.size());
    Mail teamInviteEmail = sent.get(0);
    assertEquals(MailingConstants.FROM_SUPPORT, teamInviteEmail.getFrom());

    Document doc = Jsoup.parse(teamInviteEmail.getHtml());
    Elements link = doc.select("a");
    String acceptInviteUrl = link.attr("href");

    // extract the token
    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(acceptInviteUrl);
    tokenMatcher.matches();
    String token = tokenMatcher.group(1);

    assertEquals(acceptInviteUrl, "https://www.insight.io/accept-invite?token=" + token);

    // resend the invite email
    given()
        .header("referer", "https://www.insight.io")
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, getSessionId())
        .post(String.join("/", TeamInviteResource.PATH, token, "send"))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    assertEquals(2, mailbox.getMessagesSentTo(invitedUserEmail).size());
    acceptInviteUrl = Jsoup.parse(sent.get(1).getHtml()).select("a").attr("href");
    assertEquals(acceptInviteUrl, "https://www.insight.io/accept-invite?token=" + token);
  }
}
