package com.meemaw.auth.organization.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OrganizationResourceImplTest {

  private static final String GET_ORGANIZATION_MEMBERS_PATH =
      String.join("/", OrganizationResource.PATH, "members");

  @Test
  public void get_organization_should_fail_when_no_auth() {
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
  public void get_organization_should_work_when_existing_user() {
    DataResponse<OrganizationDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
            .get(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals("000000", dataResponse.getData().getId());
    assertEquals("Insight", dataResponse.getData().getName());
  }

  @Test
  public void get_organization_members_should_fail_when_no_auth() {
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
  public void get_organization_members_should_fail_when_random_session_id() {
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
  public void get_organization_members_should_return_on_exiting_user() {
    DataResponse<List<UserDTO>> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
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
}
