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
import com.meemaw.shared.SharedConstants;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.setup.EmailTestUtils;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.model.auth.UserData;
import com.rebrowse.model.organization.PasswordPolicy;
import com.rebrowse.model.organization.PasswordPolicyCreateParams;
import com.rebrowse.model.organization.TeamInvite;
import com.rebrowse.model.organization.TeamInviteCreateParams;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OrganizationTeamInviteResourceImplTest extends AbstractAuthApiTest {

  private static final String COUNT_PATH = OrganizationTeamInviteResource.PATH + "/count";

  @Test
  public void count__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, COUNT_PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, COUNT_PATH);
  }

  @Test
  public void get__should_throw__when_non_uuid_id() {
    given()
        .when()
        .get(OrganizationTeamInviteResource.PATH + "/random")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @Test
  public void get__should_throw__when_random_id() {
    given()
        .when()
        .get(OrganizationTeamInviteResource.PATH + "/" + UUID.randomUUID())
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void invite__should_fail_when__invalid_content_type() {
    given()
        .when()
        .contentType(MediaType.TEXT_PLAIN)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void invite__should_fail__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.POST, OrganizationTeamInviteResource.PATH, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.POST, OrganizationTeamInviteResource.PATH, ContentType.JSON);
  }

  @Test
  public void invite__should_fail__when_no_payload() {
    String sessionId = authApi().loginWithAdminUser();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));

    String authToken = authApi().createApiKey(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void invite__should_fail__when_empty_payload() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"role\":\"Required\",\"email\":\"Required\"}}}"));

    String authToken = authApi().createApiKey(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body("{}")
        .post(OrganizationTeamInviteResource.PATH)
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

    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"role\":\"Invalid Value\"}}}"));

    String authToken = authApi().createApiKey(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(payload)
        .post(OrganizationTeamInviteResource.PATH)
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

    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));

    String authToken = authApi().createApiKey(sessionId);
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(payload)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void invite__should_fail__when_user_already_in_organization()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@gmail.com", password);
    String sessionId = authApi().signUpAndLogin(email, password);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(new TeamInviteCreateDTO(email, UserRole.ADMIN)))
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"email\":\"User with provided email is already in your organization\"}}}"));
  }

  @Test
  public void invite_flow__should_succeed__when_valid_payload() throws IOException {
    String email = String.format("%s@gmail.com", UUID.randomUUID());
    String payload =
        objectMapper.writeValueAsString(new TeamInviteCreateDTO(email, UserRole.ADMIN));
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    UserData userData =
        UserData.retrieve(authApi().sdkRequest().sessionId(sessionId).build())
            .toCompletableFuture()
            .join();

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(201);

    // creating same invite twice should fail with 409 Conflict
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(payload)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(409)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":409,\"reason\":\"Conflict\",\"message\":\"Conflict\",\"errors\":{\"email\":\"User with provided email has an active outstanding invite\"}}}"));

    // accept the invite
    List<Mail> sent = mailbox.getMessagesSentTo(email);
    assertEquals(1, sent.size());
    Mail actual = sent.get(0);
    assertEquals(MailingConstants.FROM_SUPPORT, actual.getFrom());

    UUID token = UUID.fromString(EmailTestUtils.parseConfirmationToken(actual));

    TeamInvite invite =
        TeamInvite.retrieve(token, authApi().sdkRequest().build()).toCompletableFuture().join();

    assertEquals(email, invite.getEmail());
    assertEquals(userData.getOrganization().getId(), invite.getOrganizationId());
    assertEquals(userData.getUser().getId(), invite.getCreator());

    String inviteAcceptPayload =
        objectMapper.writeValueAsString(
            new TeamInviteAcceptDTO("Marko Novak", "superDuperPassword123"));

    String acceptedUserSessionId =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(inviteAcceptPayload)
            .post(String.format("%s/%s/accept", OrganizationTeamInviteResource.PATH, token))
            .then()
            .statusCode(200)
            .cookie(SsoSession.COOKIE_NAME)
            .extract()
            .detailedCookie(SsoSession.COOKIE_NAME)
            .getValue();

    assertEquals(
        "Marko Novak",
        UserData.retrieve(authApi().sdkRequest().sessionId(acceptedUserSessionId).build())
            .toCompletableFuture()
            .join()
            .getUser()
            .getFullName());
  }

  @Test
  public void list_invites__should_fail__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, OrganizationTeamInviteResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, OrganizationTeamInviteResource.PATH);
  }

  @Test
  public void list_invites__should_return_collection__when_authorized() throws IOException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String authToken = authApi().createApiKey(sessionId);
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"))
        .body("data.size()", is(0));

    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .get(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"))
        .body("data.size()", is(0));

    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":0}"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(
            objectMapper.writeValueAsString(
                new TeamInviteCreateDTO(UUID.randomUUID() + "@gmail.com", UserRole.MEMBER)))
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(201);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .body(
            objectMapper.writeValueAsString(
                new TeamInviteCreateDTO(UUID.randomUUID() + "@gmail.com", UserRole.MEMBER)))
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(201);

    Response response =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(OrganizationTeamInviteResource.PATH)
            .then()
            .statusCode(200)
            .body("data.size()", is(2))
            .extract()
            .response();

    UUID firstInviteToken = UUID.fromString(response.body().path("data[0].token"));
    UUID secondInviteToken = UUID.fromString(response.body().path("data[1].token"));

    // delete first created invite
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .pathParam("token", firstInviteToken)
        .delete(OrganizationTeamInviteResource.PATH + "/{token}")
        .then()
        .statusCode(204);

    // delete second created invite
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .pathParam("token", secondInviteToken)
        .delete(OrganizationTeamInviteResource.PATH + "/{token}")
        .then()
        .statusCode(204);

    // should return 0 invites now
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"))
        .body("data.size()", is(0));
  }

  @Test
  public void delete_invite__should_fail__when_no_token_param() {
    given()
        .when()
        .delete(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(405)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":405,\"reason\":\"Method Not Allowed\",\"message\":\"Method Not Allowed\"}}"));
  }

  @Test
  public void delete_invite__should_fail_when__unauthorized() {
    String path = OrganizationTeamInviteResource.PATH + "/" + UUID.randomUUID();
    RestAssuredUtils.ssoSessionCookieTestCases(Method.DELETE, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.DELETE, path);
  }

  @Test
  public void delete_invite__should_fail__when_invalid_token_param() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .pathParam("token", "randomToken")
        .delete(OrganizationTeamInviteResource.PATH + "/{token}")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @Test
  public void send_invite__should_fail__when_unauthorized() {
    String path =
        String.join("/", OrganizationTeamInviteResource.PATH, UUID.randomUUID().toString(), "send");

    RestAssuredUtils.ssoSessionCookieTestCases(Method.POST, path, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.POST, path, ContentType.JSON);
  }

  @Test
  public void send_invite_flow__should_succeed__when_existing_invite()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String invitedUserEmail = String.format("%s@gmail.com", UUID.randomUUID());
    String invitePayload =
        objectMapper.writeValueAsString(new TeamInviteCreateDTO(invitedUserEmail, UserRole.ADMIN));

    String referrer = String.format("https://www.%s", SharedConstants.REBROWSE_STAGING_DOMAIN);

    // Invite the user
    given()
        .header(io.vertx.core.http.HttpHeaders.REFERER.toString(), referrer)
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(invitePayload)
        .post(OrganizationTeamInviteResource.PATH)
        .then()
        .statusCode(201);

    List<Mail> sent = mailbox.getMessagesSentTo(invitedUserEmail);
    assertEquals(1, sent.size());
    Mail teamInviteEmail = sent.get(0);
    assertEquals(MailingConstants.FROM_SUPPORT, teamInviteEmail.getFrom());

    String acceptInviteUrl = EmailTestUtils.parseLink(teamInviteEmail);
    String token = EmailTestUtils.parseConfirmationToken(acceptInviteUrl);

    assertEquals(acceptInviteUrl, referrer + "/accept-invite?token=" + token);

    // resend the invite email
    DataResponse<TeamInviteDTO> dataResponse =
        given()
            .header(io.vertx.core.http.HttpHeaders.REFERER.toString(), referrer)
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .post(String.join("/", OrganizationTeamInviteResource.PATH, token, "send"))
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(new TypeRef<>() {});

    UserData userData = authApi().retrieveUserData(sessionId);

    assertEquals(dataResponse.getData().getRole(), UserRole.ADMIN);
    assertEquals(dataResponse.getData().getEmail(), invitedUserEmail);
    assertEquals(dataResponse.getData().getCreator(), userData.getUser().getId());
    assertEquals(
        dataResponse.getData().getOrganizationId(), userData.getUser().getOrganizationId());

    assertEquals(2, mailbox.getMessagesSentTo(invitedUserEmail).size());
    acceptInviteUrl = Jsoup.parse(sent.get(1).getHtml()).select("a").attr("href");
    assertEquals(acceptInviteUrl, referrer + "/accept-invite?token=" + token);
  }

  @Test
  public void accept__should_fail__when_organization_password_policy_violated()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String newUserEmail = String.format("%s@gmail.com", UUID.randomUUID());

    PasswordPolicy.create(
            PasswordPolicyCreateParams.builder()
                .minCharacters((short) 15)
                .requireNonAlphanumericCharacter(true)
                .build(),
            authApi().sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();

    TeamInvite teamInvite =
        TeamInvite.create(
                TeamInviteCreateParams.builder()
                    .email(newUserEmail)
                    .role(com.rebrowse.model.user.UserRole.MEMBER)
                    .build(),
                authApi().sdkRequest().sessionId(sessionId).build())
            .toCompletableFuture()
            .join();

    String acceptPath =
        String.format("%s/%s/accept", OrganizationTeamInviteResource.PATH, teamInvite.getToken());

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(new TeamInviteAcceptDTO("Matej", "password123")))
        .post(acceptPath)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"password\":\"Password should contain at least 15 characters\"}}}"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            objectMapper.writeValueAsString(new TeamInviteAcceptDTO("Matej", "password123456789")))
        .post(acceptPath)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"password\":\"Password should contain at least one non-alphanumeric character\"}}}"));

    String acceptedUserSessionId =
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                objectMapper.writeValueAsString(
                    new TeamInviteAcceptDTO("Bruce Lee", "password123456789!")))
            .post(acceptPath)
            .then()
            .statusCode(200)
            .cookie(SsoSession.COOKIE_NAME)
            .extract()
            .detailedCookie(SsoSession.COOKIE_NAME)
            .getValue();

    assertEquals(
        "Bruce Lee",
        UserData.retrieve(authApi().sdkRequest().sessionId(acceptedUserSessionId).build())
            .toCompletableFuture()
            .join()
            .getUser()
            .getFullName());
  }
}
