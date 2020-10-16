package com.meemaw.auth.organization.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteCreateDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.SessionInfoDTO;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.Mail;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OrganizationInviteResourceImplTest extends AbstractAuthApiTest {

  @Test
  public void invite__should_fail_when__invalid_content_type() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void invite__should_fail__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.POST, OrganizationInviteResource.PATH, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.POST, OrganizationInviteResource.PATH, ContentType.JSON);
  }

  @Test
  public void invite__should_fail__when_no_payload() {
    String sessionId = authApi().loginWithInsightAdmin();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void invite__should_fail__when_empty_payload() {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"role\":\"Required\",\"email\":\"Required\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body("{}")
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"role\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void invite__should_fail__when_invalid_role() throws URISyntaxException, IOException {
    String payload =
        Files.readString(Path.of(getClass().getResource("/org/invite/invalidRole.json").toURI()));

    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"role\":\"Invalid Value\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(payload)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"role\":\"Invalid Value\"}}}"));
  }

  @Test
  public void invite__should_fail__when_invalid_email() throws IOException {
    String payload =
        objectMapper.writeValueAsString(new TeamInviteCreateDTO("notEmail", UserRole.ADMIN));

    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(payload)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void invite_flow__should_succeed__when_valid_payload() throws IOException {
    String payload =
        objectMapper.writeValueAsString(
            new TeamInviteCreateDTO("test-team-invitation@gmail.com", UserRole.ADMIN));
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(201);

    // creating same invite twice should fail with 409 Conflict
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(409)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":409,\"reason\":\"Conflict\",\"message\":\"Conflict\"}}"));

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
            new TeamInviteAcceptDTO("Marko Novak", "superDuperPassword123"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(inviteAcceptPayload)
        .post(String.join("/", OrganizationInviteResource.PATH, token, "accept"))
        .then()
        .statusCode(204);
  }

  @Test
  public void list_invites__should_fail__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, OrganizationInviteResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, OrganizationInviteResource.PATH);
  }

  @Test
  public void list_invites__should_return_collection__when_authorized() throws IOException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(OrganizationInviteResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"))
        .body("data.size()", is(0));

    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .get(OrganizationInviteResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"))
        .body("data.size()", is(0));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(
            objectMapper.writeValueAsString(
                new TeamInviteCreateDTO(UUID.randomUUID() + "@gmail.com", UserRole.MEMBER)))
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(201);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(
            objectMapper.writeValueAsString(
                new TeamInviteCreateDTO(UUID.randomUUID() + "@gmail.com", UserRole.MEMBER)))
        .post(OrganizationInviteResource.PATH)
        .then()
        .statusCode(201);

    Response response =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(OrganizationInviteResource.PATH);
    response.then().statusCode(200).body("data.size()", is(2));

    UUID firstInviteToken = UUID.fromString(response.body().path("data[0].token"));
    UUID secondInviteToken = UUID.fromString(response.body().path("data[1].token"));

    // delete first created invite
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .pathParam("token", firstInviteToken)
        .delete(OrganizationInviteResource.PATH + "/{token}")
        .then()
        .statusCode(204);

    // delete second created invite
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .pathParam("token", secondInviteToken)
        .delete(OrganizationInviteResource.PATH + "/{token}")
        .then()
        .statusCode(204);

    // should return 0 invites now
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(OrganizationInviteResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"))
        .body("data.size()", is(0));
  }

  @Test
  public void delete_invite__should_fail__when_no_token_param() {
    given()
        .when()
        .delete(OrganizationInviteResource.PATH)
        .then()
        .statusCode(405)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":405,\"reason\":\"Method Not Allowed\",\"message\":\"Method Not Allowed\"}}"));
  }

  @Test
  public void delete_invite__should_fail_when__unauthorized() {
    String path = OrganizationInviteResource.PATH + "/" + UUID.randomUUID();
    RestAssuredUtils.ssoSessionCookieTestCases(Method.DELETE, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.DELETE, path);
  }

  @Test
  public void delete_invite__should_fail__when_invalid_token_param() {
    String sessionId = authApi().loginWithInsightAdmin();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .pathParam("token", "randomToken")
        .delete(OrganizationInviteResource.PATH + "/{token}")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @Test
  public void send_invite__should_fail__when_unauthorized() {
    String path =
        String.join("/", OrganizationInviteResource.PATH, UUID.randomUUID().toString(), "send");

    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, path, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, path, ContentType.JSON);
  }

  @Test
  public void send_invite_flow__should_succeed__when_existing_invite()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    SessionInfoDTO sessionInfoDTO = authApi().getSessionInfo(sessionId).get();
    String invitedUserEmail = "send-invite-flow@gmail.com";
    String invitePayload =
        objectMapper.writeValueAsString(new TeamInviteCreateDTO(invitedUserEmail, UserRole.ADMIN));

    // Invite the user
    given()
        .header("referer", "https://www.insight.io")
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(invitePayload)
        .post(OrganizationInviteResource.PATH)
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
    DataResponse<TeamInviteDTO> dataResponse =
        given()
            .header("referer", "https://www.insight.io")
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .post(String.join("/", OrganizationInviteResource.PATH, token, "send"))
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(new TypeRef<>() {});

    assertEquals(dataResponse.getData().getRole(), UserRole.ADMIN);
    assertEquals(dataResponse.getData().getEmail(), invitedUserEmail);
    assertEquals(dataResponse.getData().getCreator(), sessionInfoDTO.getUser().getId());
    assertEquals(
        dataResponse.getData().getOrganizationId(), sessionInfoDTO.getUser().getOrganizationId());

    assertEquals(2, mailbox.getMessagesSentTo(invitedUserEmail).size());
    acceptInviteUrl = Jsoup.parse(sent.get(1).getHtml()).select("a").attr("href");
    assertEquals(acceptInviteUrl, "https://www.insight.io/accept-invite?token=" + token);
  }
}
