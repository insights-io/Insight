package com.meemaw.auth.organization.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.billing.model.SubscriptionPlan;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class OrganizationResourceImplTest {

  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;

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
            .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
            .get(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals("000000", dataResponse.getData().getId());
    assertEquals("Insight", dataResponse.getData().getName());
    assertEquals(SubscriptionPlan.ENTERPRISE, dataResponse.getData().getPlan());
  }

  @Test
  public void get_organization__should_return_organization_with_free_plan__when_new_user()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String sessionId =
        SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, password + "@gmail.com", password);

    DataResponse<OrganizationDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(SubscriptionPlan.FREE, dataResponse.getData().getPlan());
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
