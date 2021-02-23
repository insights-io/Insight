package com.rebrowse.auth.organization.resource.v1;

import static com.rebrowse.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;
import static com.rebrowse.shared.rest.query.AbstractQueryParser.QUERY_PARAM;
import static com.rebrowse.shared.rest.query.AbstractQueryParser.SORT_BY_PARAM;
import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.auth.organization.model.AvatarType;
import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.auth.organization.model.dto.AvatarSetupDTO;
import com.rebrowse.auth.organization.model.dto.OrganizationDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.user.datasource.UserTable;
import com.rebrowse.auth.user.model.UserRole;
import com.rebrowse.auth.user.model.dto.UserDTO;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.shared.SharedConstants;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import com.rebrowse.api.query.TermCondition;
import com.rebrowse.model.auth.UserData;
import com.rebrowse.model.organization.TeamInvite;
import com.rebrowse.model.organization.TeamInviteAcceptParams;
import com.rebrowse.model.organization.TeamInviteCreateParams;
import com.rebrowse.model.user.User;
import com.rebrowse.model.user.UserUpdateParams;
import com.rebrowse.net.RequestOptions;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class OrganizationResourceImplTest extends AbstractAuthApiQuarkusTest {

  @TestHTTPResource(OrganizationResource.PATH + "/avatar")
  URL organizationAvatarEndpoint;

  @TestHTTPResource(OrganizationResource.PATH + "/members")
  URL organizationMembersEndpoint;

  @TestHTTPResource(OrganizationResource.PATH + "/members/count")
  URL organizationMembersCountEndpoint;

  @Test
  public void count_members__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.GET, organizationMembersCountEndpoint.toString());
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.GET, organizationMembersCountEndpoint.toString());
  }

  @Test
  public void delete__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.DELETE, OrganizationResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.DELETE, OrganizationResource.PATH);
  }

  @Test
  public void delete__should_throw__when_performed_by_non_admin_user()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    User.update(
            UserUpdateParams.builder().role(com.rebrowse.model.user.UserRole.MEMBER).build(),
            sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(OrganizationResource.PATH)
        .then()
        .statusCode(403)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":403,\"reason\":\"Forbidden\",\"message\":\"Forbidden\"}}"));
  }

  @Test
  public void delete__should_delete_all_users_and_clear_caches__when_valid()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String apiKey = authorizationFlows().createApiKey(sessionId).getToken();
    String newUserPassword = UUID.randomUUID().toString();
    String newUserEmail = AuthApiTestUtils.randomBusinessEmail();
    RequestOptions options = sdkRequest().sessionId(sessionId).build();

    TeamInvite teamInvite =
        TeamInvite.create(
                TeamInviteCreateParams.builder()
                    .role(com.rebrowse.model.user.UserRole.ADMIN)
                    .email(newUserEmail)
                    .build(),
                options)
            .toCompletableFuture()
            .join();

    teamInvite
        .accept(
            TeamInviteAcceptParams.builder()
                .fullName("Marko Skace")
                .password(newUserPassword)
                .redirect(GlobalTestData.LOCALHOST_REDIRECT_URL)
                .build(),
            options)
        .toCompletableFuture()
        .join();

    String secondUserSessionId = authorizationFlows().login(newUserEmail, newUserPassword);
    String secondUserApiKey = authorizationFlows().createApiKey(secondUserSessionId).getToken();

    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondUserApiKey)
        .delete(OrganizationResource.PATH)
        .then()
        .statusCode(204);

    // Should purge session cache
    List.of(sessionId, secondUserSessionId)
        .forEach(
            session ->
                given()
                    .when()
                    .cookie(SsoSession.COOKIE_NAME, session)
                    .get(OrganizationResource.PATH)
                    .then()
                    .statusCode(401));

    // Should delete auth token
    List.of(apiKey, secondUserApiKey)
        .forEach(
            token ->
                given()
                    .when()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .get(OrganizationResource.PATH)
                    .then()
                    .statusCode(401));
  }

  @Test
  public void setup_avatar_associated_organization__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.PATCH, organizationAvatarEndpoint.toString(), ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.PATCH, organizationAvatarEndpoint.toString(), ContentType.JSON);
  }

  @Test
  public void setup_avatar_associated_organization__should_throw__when_no_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .patch(OrganizationResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void setup_avatar_associated_organization__should_throw__when_empty_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .patch(organizationAvatarEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"type\":\"Required\"}}}"));
  }

  @Test
  public void setup_avatar_associated_organization__should_throw__when_invalid_type() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{\"type\": \"random\"}")
        .patch(organizationAvatarEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"type\":\"Invalid Value\"}}}"));
  }

  @Test
  public void setup_avatar_associated_organization__should_throw__when_avatar_without_image()
      throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    AvatarSetupDTO avatarSetup = new AvatarSetupDTO(AvatarType.AVATAR, null);

    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(avatarSetup))
        .patch(organizationAvatarEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"image\":\"Required\"}}}"));
  }

  @Test
  public void setup_avatar_associated_organization__should_work__when_initials()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    AvatarSetupDTO avatarSetup = new AvatarSetupDTO(AvatarType.INITIALS, null);
    DataResponse<OrganizationDTO> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(objectMapper.writeValueAsString(avatarSetup))
            .patch(organizationAvatarEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(avatarSetup, dataResponse.getData().getAvatar());
  }

  @Test
  public void setup_avatar_associated_organization__should_work__when_avatar_with_image()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    AvatarSetupDTO avatarSetup = new AvatarSetupDTO(AvatarType.AVATAR, "image");

    String apiKey = authorizationFlows().createApiKey(sessionId).getToken();
    DataResponse<OrganizationDTO> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .body(objectMapper.writeValueAsString(avatarSetup))
            .patch(organizationAvatarEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(avatarSetup, dataResponse.getData().getAvatar());
  }

  @Test
  public void patch_associated_organization__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.PATCH, OrganizationResource.PATH, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.PATCH, OrganizationResource.PATH, ContentType.JSON);
  }

  @Test
  public void patch_associated_organization__should_throw__when_empty_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body("{}")
        .patch(OrganizationResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void patch_associated_organization__should_throw__when_no_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .patch(OrganizationResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void patch_associated_organization__should_throw__when_invalid_body()
      throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(Map.of("field1", "value1")))
        .patch(OrganizationResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"field1\":\"Unexpected field\"}}}"));
  }

  @Test
  @Disabled
  public void patch_associated_organization__should_throw__when_invalid_role()
      throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(Map.of("defaultRole", "value1")))
        .patch(OrganizationResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"defaultRole\":\"Unexpected field\"}}}"));
  }

  @Test
  public void patch_associated_organization__should_work__when_valid_body()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    DataResponse<OrganizationDTO> organizationDataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(
                objectMapper.writeValueAsString(
                    Map.of("name", "My new name", "defaultRole", "admin")))
            .patch(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals("My new name", organizationDataResponse.getData().getName());
    assertEquals(UserRole.ADMIN, organizationDataResponse.getData().getDefaultRole());

    String apiKey = authorizationFlows().createApiKey(sessionId).getToken();
    organizationDataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .body(
                objectMapper.writeValueAsString(
                    Map.of("name", "My new name 2", "default_role", "member")))
            .patch(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals("My new name 2", organizationDataResponse.getData().getName());
    assertEquals(UserRole.MEMBER, organizationDataResponse.getData().getDefaultRole());
  }

  @Test
  public void get_associated_organization__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, OrganizationResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, OrganizationResource.PATH);
  }

  @Test
  public void get_associated_organization__should_work__when_existing_user() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    DataResponse<OrganizationDTO> firstResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(REBROWSE_ORGANIZATION_ID, firstResponse.getData().getId());
    assertEquals(SharedConstants.REBROWSE_ORGANIZATION_NAME, firstResponse.getData().getName());

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    DataResponse<OrganizationDTO> secondResponse =
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .get(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(firstResponse, secondResponse);
  }

  @Test
  public void get_organization_members__should_fail__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, organizationMembersEndpoint.toString());
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, organizationMembersEndpoint.toString());
  }

  @Test
  public void get_organization_members__should_return__when_existing_user() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    DataResponse<List<UserDTO>> firstResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(organizationMembersEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(GlobalTestData.REBROWSE_ADMIN_EMAIL, firstResponse.getData().get(0).getEmail());
    assertEquals(
        GlobalTestData.REBROWSE_ADMIN_FULL_NAME, firstResponse.getData().get(0).getFullName());
    assertEquals(UserRole.ADMIN, firstResponse.getData().get(0).getRole());

    // Search by query
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(QUERY_PARAM, "random")
        .queryParam(SORT_BY_PARAM, "-created_at")
        .get(organizationMembersEndpoint)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(QUERY_PARAM, "random")
        .get(organizationMembersCountEndpoint)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":0}}"));

    DataResponse<List<UserDTO>> fullNameResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .queryParam(QUERY_PARAM, GlobalTestData.REBROWSE_ADMIN_FULL_NAME)
            .queryParam(SORT_BY_PARAM, UserTable.CREATED_AT)
            .get(organizationMembersEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(new TypeRef<>() {});

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(QUERY_PARAM, GlobalTestData.REBROWSE_ADMIN_FULL_NAME)
        .get(organizationMembersCountEndpoint)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":1}}"));

    assertEquals(firstResponse, fullNameResponse);

    // Search by role
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(UserTable.ROLE, TermCondition.EQ.rhs("member"))
        .get(organizationMembersEndpoint)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));

    DataResponse<List<UserDTO>> roleResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .queryParam(UserTable.ROLE, TermCondition.EQ.rhs("admin"))
            .get(organizationMembersEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(new TypeRef<>() {});

    assertEquals(firstResponse, roleResponse);

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    DataResponse<List<UserDTO>> secondResponse =
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .get(organizationMembersEndpoint)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(firstResponse, secondResponse);
  }

  @Test
  public void get_organization__should_fail__when_unauthorized() {
    String path = OrganizationResource.PATH + "/" + Organization.identifier();
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, path);
  }

  @Test
  public void get_organization__should_work__when_authenticated() throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    UserData userData = authorizationFlows().retrieveUserData(sessionId);
    User user = userData.getUser();

    DataResponse<OrganizationDTO> firstResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(OrganizationResource.PATH + "/" + user.getOrganizationId())
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(user.getOrganizationId(), firstResponse.getData().getId());

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    DataResponse<OrganizationDTO> secondResponse =
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .get(OrganizationResource.PATH + "/" + user.getOrganizationId())
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(firstResponse, secondResponse);
  }

  @Test
  public void get_organization__should_throw__when_trying_to_retrieve_someone_elses_organization()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(OrganizationResource.PATH + "/000000")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .get(OrganizationResource.PATH + "/000000")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }
}
