package com.meemaw.session.sessions.resource.v1;

import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.FIELDS;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.TABLE;
import static com.meemaw.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;
import static com.meemaw.shared.rest.query.AbstractQueryParser.GROUP_BY_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.LIMIT_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.SORT_BY_PARAM;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.session.model.CreatePageVisitDTO;
import com.meemaw.session.model.PageVisitSessionLink;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionTable;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.shared.sql.SQLContext;
import com.meemaw.test.rest.data.UserAgentData;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.rebrowse.api.query.TermCondition;
import com.rebrowse.model.organization.Organization;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.ValidatableResponse;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.ws.rs.core.HttpHeaders;
import org.jooq.Query;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(AuthApiTestResource.class)
public class PageVisitResourceImplTest extends AbstractSessionResourceTest {

  private CreatePageVisitDTO withUpdateOrganizationId(
      CreatePageVisitDTO createPageVisitDTO, String organizationId) {
    return new CreatePageVisitDTO(
        organizationId,
        createPageVisitDTO.getDeviceId(),
        createPageVisitDTO.getUrl(),
        createPageVisitDTO.getReferrer(),
        createPageVisitDTO.getDoctype(),
        createPageVisitDTO.getScreenWidth(),
        createPageVisitDTO.getScreenHeight(),
        createPageVisitDTO.getWidth(),
        createPageVisitDTO.getHeight(),
        createPageVisitDTO.getCompiledTs());
  }

  private CreatePageVisitDTO readSimplePage() throws URISyntaxException, IOException {
    return objectMapper.readValue(
        Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI())),
        CreatePageVisitDTO.class);
  }

  @Test
  public void post_v1_pages__should_throw_error__when_unsupported_media_type() {
    given()
        .when()
        .contentType(TEXT_PLAIN)
        .post(PageVisitResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"message\":\"Media type not supported.\",\"reason\":\"Unsupported Media Type\",\"statusCode\":415}}"));
  }

  @Test
  public void post_v1_pages__should_throw_error__when_empty_payload() {
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .post(PageVisitResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void post_v1_pages__should_throw__when_empty_json() {
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body("{}")
        .post(PageVisitResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"doctype\":\"may not be null\",\"organizationId\":\"Required\",\"url\":\"may not be null\"}}}"));
  }

  @Test
  public void post_v1_pages__should_throw_error__when_robot_user_agent()
      throws URISyntaxException, IOException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(payload)
        .post(PageVisitResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"You're a robot\"}}"));
  }

  @Test
  public void post_v1_pages__should_throw_error__when_hacker_user_agent()
      throws URISyntaxException, IOException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    given()
        .when()
        .contentType(APPLICATION_JSON)
        .header(HttpHeaders.USER_AGENT, "*".repeat(200))
        .body(payload)
        .post(PageVisitResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"You're a robot\"}}"));
  }

  @Test
  public void post_v1_pages__should_throw_error__when_empty_user_agent()
      throws URISyntaxException, IOException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    given()
        .when()
        .contentType(APPLICATION_JSON)
        .header(HttpHeaders.USER_AGENT, "")
        .body(payload)
        .post(PageVisitResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"userAgent\":\"Required\"}}}"));
  }

  @Test
  public void get_v1_pages_id__should_throw__when_unauthorized() {
    String path = String.format("%s/%s", PageVisitResource.PATH, UUID.randomUUID());
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, path);
  }

  @Test
  public void get_v1_pages_id__should_throw__when_page_not_existing() {
    String path = String.format("%s/%s", PageVisitResource.PATH, UUID.randomUUID());
    given()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .when()
        .get(path)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void post_v1_pages__should_throw__when_free_quota_exceeded()
      throws IOException, URISyntaxException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    Organization organization = authApi().retrieveOrganization(sessionId);
    CreatePageVisitDTO withFreePlanOrganization =
        withUpdateOrganizationId(readSimplePage(), organization.getId());
    String body = objectMapper.writeValueAsString(withFreePlanOrganization);

    Function<Integer, ValidatableResponse> createPage =
        (statusCode) ->
            given()
                .when()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
                .body(body)
                .post(PageVisitResource.PATH)
                .then()
                .statusCode(statusCode);

    // TODO: execute those in parallel (there were some issued with deadlock / thread saturation)
    for (int i = 0; i < 1000; i++) {
      createPage.apply(200);
    }

    createPage
        .apply(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Free plan quota reached. Please upgrade your plan to continue collecting Insights.\"}}"));
  }

  @Test
  public void post_v1_pages__should_throw__when_missing_organization()
      throws IOException, URISyntaxException {
    CreatePageVisitDTO withRandomOrganization =
        withUpdateOrganizationId(
            readSimplePage(), com.meemaw.auth.organization.model.Organization.identifier());

    given()
        .when()
        .contentType(ContentType.JSON)
        .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
        .body(objectMapper.writeValueAsString(withRandomOrganization))
        .post(PageVisitResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void post_v1_pages__should_link_sessions__when_matching_device_id()
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    // Create first session
    DataResponse<PageVisitSessionLink> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .body(payload)
            .post(PageVisitResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    PageVisitSessionLink pageVisitSessionLink = dataResponse.getData();
    UUID deviceId = pageVisitSessionLink.getDeviceId();
    UUID sessionId = pageVisitSessionLink.getSessionId();
    UUID pageId = pageVisitSessionLink.getPageVisitId();

    // GET newly created page
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .get(String.format("%s/%s", PageVisitResource.PATH, pageId))
        .then()
        .statusCode(200)
        .body("data.organizationId", is(REBROWSE_ORGANIZATION_ID))
        .body("data.sessionId", is(sessionId.toString()));

    // GET newly created session
    DataResponse<SessionDTO> sessionDataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
            .get(String.format("%s/%s", SessionResource.PATH, sessionId))
            .then()
            .statusCode(200)
            .body("data.organizationId", is(REBROWSE_ORGANIZATION_ID))
            .body("data.deviceId", is(deviceId.toString()))
            .body("data.id", is(sessionId.toString()))
            .extract()
            .response()
            .as(new TypeRef<>() {});

    // GET sessions
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam(
            SessionTable.CREATED_AT,
            TermCondition.GTE.rhs(sessionDataResponse.getData().getCreatedAt()))
        .get(SessionResource.PATH)
        .then()
        .statusCode(200)
        .body("data.size()", is(1));

    ObjectNode nextPageNode = objectMapper.readValue(payload, ObjectNode.class);
    nextPageNode.put("deviceId", deviceId.toString());

    // page is linked to same session because we pass device id along
    dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .body(nextPageNode)
            .post(PageVisitResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertNotEquals(dataResponse.getData().getPageVisitId(), pageId);
    assertEquals(dataResponse.getData().getDeviceId(), deviceId);
    assertEquals(dataResponse.getData().getSessionId(), sessionId);

    // GET sessions again to confirm no new sessions has been created
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam(
            SessionTable.CREATED_AT,
            TermCondition.GTE.rhs(sessionDataResponse.getData().getCreatedAt()))
        .get(SessionResource.PATH)
        .then()
        .statusCode(200)
        .body("data.size()", is(1));
  }

  @Test
  public void post_v1_pages__should_create_new_session__when_no_matching_device_id()
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    // page and session are created
    DataResponse<PageVisitSessionLink> dataResponse =
        given()
            .when()
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .contentType(ContentType.JSON)
            .body(payload)
            .post(PageVisitResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    PageVisitSessionLink pageVisitSessionLink = dataResponse.getData();
    UUID deviceId = pageVisitSessionLink.getDeviceId();
    UUID sessionId = pageVisitSessionLink.getSessionId();

    // new page is created cause device id is not matching
    dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .body(payload)
            .post(PageVisitResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    UUID pageId = pageVisitSessionLink.getPageVisitId();
    assertNotEquals(dataResponse.getData().getDeviceId(), deviceId);
    assertNotEquals(dataResponse.getData().getSessionId(), sessionId);
    assertNotEquals(dataResponse.getData().getPageVisitId(), pageId);

    DataResponse<SessionDTO> firstSessionDataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
            .get(String.format("%s/%s", SessionResource.PATH, sessionId))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    // GET sessions should return 2 newly created sessions
    DataResponse<List<SessionDTO>> sessions =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
            .queryParam(
                SessionTable.CREATED_AT,
                TermCondition.GTE.rhs(firstSessionDataResponse.getData().getCreatedAt()))
            .get(SessionResource.PATH)
            .then()
            .statusCode(200)
            .body("data.size()", is(2))
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(DESKTOP_MAC_CHROME, sessions.getData().get(0).getUserAgent());
    assertEquals(BOYDTON_US_VIRGINIA_NA, sessions.getData().get(0).getLocation());
  }

  @Test
  public void post_v1_pages__should_create_new_session__when_no_session_timeout()
      throws IOException, URISyntaxException {
    ObjectNode payload =
        objectMapper.readValue(
            Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI())),
            ObjectNode.class);

    UUID deviceId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    String organizationId = payload.get("organizationId").textValue();

    payload.put("deviceId", deviceId.toString());

    Query query =
        SQLContext.POSTGRES
            .insertInto(TABLE)
            .columns(FIELDS)
            .values(
                sessionId,
                deviceId,
                organizationId,
                JsonObject.mapFrom(BOYDTON_US_VIRGINIA_NA),
                JsonObject.mapFrom(DESKTOP_MAC_CHROME),
                OffsetDateTime.ofInstant(
                    Instant.now().minus(31, ChronoUnit.MINUTES), ZoneOffset.UTC))
            .returning(FIELDS);

    sqlPool.execute(query).toCompletableFuture().join();

    // page and session are created
    DataResponse<PageVisitSessionLink> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .body(payload)
            .post(PageVisitResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertNotEquals(dataResponse.getData().getSessionId(), sessionId);
    assertEquals(dataResponse.getData().getDeviceId(), deviceId);
  }

  @Test
  public void get_v1_pages_count__should_throw__when_unauthorized() {
    String path = String.format("%s/%s", PageVisitResource.PATH, "count");
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, path);
  }

  @Test
  public void get_v1_pages_count__should_throw__when_unsupported_fields() {
    String sessionId = authApi().loginWithAdminUser();

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam("random", TermCondition.GTE.rhs("aba"))
                .queryParam("aba", "gtecaba")
                .queryParam(caseProvider.apply(GROUP_BY_PARAM), "another")
                .queryParam(caseProvider.apply(SORT_BY_PARAM), "hehe")
                .queryParam(caseProvider.apply(LIMIT_PARAM), "not_string")
                .get(String.format("%s/%s", PageVisitResource.PATH, "count"))
                .then()
                .statusCode(400)
                .body(
                    sameJson(
                        String.format(
                            "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field\",\"random\":\"Unexpected field\",\"%s\":\"Number expected\",\"%s\":{\"another\":\"Unexpected field\"},\"%s\":{\"hehe\":\"Unexpected field\"}}}}",
                            caseProvider.apply(LIMIT_PARAM),
                            caseProvider.apply(GROUP_BY_PARAM),
                            caseProvider.apply(SORT_BY_PARAM)))));
  }

  @Test
  public void get_v1_sessions_count__should_return_empty_count__when_no_page_visits()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(String.format("%s/%s", PageVisitResource.PATH, "count"))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":0}}"));
  }
}
