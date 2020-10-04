package com.meemaw.auth.organization.resource.v1;

import static com.meemaw.shared.SharedConstants.INSIGHT_ORGANIZATION_ID;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.AuthApiTestProvider.INSIGHT_ADMIN_EMAIL;
import static com.meemaw.test.setup.AuthApiTestProvider.INSIGHT_ADMIN_FULL_NAME;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.SessionInfoDTO;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Method;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OrganizationResourceImplTest extends AbstractAuthApiTest {

  private static final String GET_ORGANIZATION_MEMBERS_PATH =
      String.join("/", OrganizationResource.PATH, "members");

  @Test
  public void get_associated_organization__should_fail__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, OrganizationResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, OrganizationResource.PATH);
  }

  @Test
  public void get_associated_organization__should_work__when_existing_user() {
    String sessionId = authApi().loginWithInsightAdmin();

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

    assertEquals(INSIGHT_ORGANIZATION_ID, firstResponse.getData().getId());
    assertEquals("Insight", firstResponse.getData().getName());

    String authToken = authApi().createAuthToken(sessionId);
    DataResponse<OrganizationDTO> secondResponse =
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
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
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, GET_ORGANIZATION_MEMBERS_PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, GET_ORGANIZATION_MEMBERS_PATH);
  }

  @Test
  public void get_organization_members__should_return__when_exiting_user() {
    String sessionId = authApi().loginWithInsightAdmin();

    DataResponse<List<UserDTO>> firstResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(GET_ORGANIZATION_MEMBERS_PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(INSIGHT_ADMIN_EMAIL, firstResponse.getData().get(0).getEmail());
    assertEquals(INSIGHT_ADMIN_FULL_NAME, firstResponse.getData().get(0).getFullName());
    assertEquals(UserRole.ADMIN, firstResponse.getData().get(0).getRole());

    String authToken = authApi().createAuthToken(sessionId);
    DataResponse<List<UserDTO>> secondResponse =
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
            .get(GET_ORGANIZATION_MEMBERS_PATH)
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
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    SessionInfoDTO sessionInfo = authApi().getSessionInfo(sessionId).get();
    AuthUser user = sessionInfo.getUser();

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

    String authToken = authApi().createAuthToken(sessionId);
    DataResponse<OrganizationDTO> secondResponse =
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
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
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(OrganizationResource.PATH + "/000000")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    String authToken = authApi().createAuthToken(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .get(OrganizationResource.PATH + "/000000")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }
}
