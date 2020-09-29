package com.meemaw.auth.organization.resource.v1;

import static com.meemaw.shared.SharedConstants.INSIGHT_ORGANIZATION_ID;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.token.datasource.AuthTokenDatasource;
import com.meemaw.auth.sso.token.model.CreateAuthTokenParams;
import com.meemaw.auth.sso.token.model.dto.AuthTokenDTO;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OrganizationResourceImplTest extends AbstractAuthApiTest {

  @Inject AuthTokenDatasource authTokenDatasource;
  @Inject UserDatasource userDatasource;

  private static final String GET_ORGANIZATION_MEMBERS_PATH =
      String.join("/", OrganizationResource.PATH, "members");

  @Test
  public void get_organization__should_fail__when_no_auth() {
    given()
        .when()
        .get(OrganizationResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void get_organization_should_fail_when_random_session_id() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .get(OrganizationResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void get_organization__should_work__when_existing_user() {
    DataResponse<OrganizationDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
            .get(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(INSIGHT_ORGANIZATION_ID, dataResponse.getData().getId());
    assertEquals("Insight", dataResponse.getData().getName());
  }

  @Test
  public void get_organization_members__should_fail__when_no_auth() {
    given()
        .when()
        .get(GET_ORGANIZATION_MEMBERS_PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void get_organization_members__should_fail__when_random_session_id() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, "random")
        .get(GET_ORGANIZATION_MEMBERS_PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void get_organization_members__should_return__when_exiting_user() {
    DataResponse<List<UserDTO>> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
            .get(GET_ORGANIZATION_MEMBERS_PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals("admin@insight.io", dataResponse.getData().get(0).getEmail());
    assertEquals("Admin Admin", dataResponse.getData().get(0).getFullName());
    assertEquals(UserRole.ADMIN, dataResponse.getData().get(0).getRole());
  }

  @Test
  public void get_organization_by_id__should_fail__when_no_auth() {
    given()
        .when()
        .pathParam("organizationId", Organization.identifier())
        .get(OrganizationResource.PATH + "/{organizationId}")
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void get_organization_by_id__should_fail__when_broken_auth() {
    given()
        .when()
        .header("Authorization", "Bearer todo")
        .pathParam("organizationId", Organization.identifier())
        .get(OrganizationResource.PATH + "/{organizationId}")
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void get_organization_by_id__should_work__when_authenticated()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    AuthUser user = authApi().getUser(sessionId).get();

    AuthTokenDTO authToken =
        authTokenDatasource
            .create(CreateAuthTokenParams.builder().token("todo").userId(user.getId()).build())
            .toCompletableFuture()
            .join();

    DataResponse<OrganizationDTO> dataResponse =
        given()
            .when()
            .header("Authorization", String.format("Bearer %s", authToken.getToken()))
            .pathParam("organizationId", user.getOrganizationId())
            .get(OrganizationResource.PATH + "/{organizationId}")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(user.getOrganizationId(), dataResponse.getData().getId());
  }
}
