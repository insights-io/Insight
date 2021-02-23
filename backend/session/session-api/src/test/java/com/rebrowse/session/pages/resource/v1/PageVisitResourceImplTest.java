package com.rebrowse.session.pages.resource.v1;

import static com.rebrowse.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;
import static com.rebrowse.shared.rest.query.AbstractQueryParser.GROUP_BY_PARAM;
import static com.rebrowse.shared.rest.query.AbstractQueryParser.LIMIT_PARAM;
import static com.rebrowse.shared.rest.query.AbstractQueryParser.SORT_BY_PARAM;
import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rebrowse.session.model.PageVisitCreateParams;
import com.rebrowse.session.model.PageVisitSessionLink;
import com.rebrowse.session.model.SessionDTO;
import com.rebrowse.session.pages.datasource.PageVisitTable;
import com.rebrowse.session.sessions.datasource.SessionTable;
import com.rebrowse.session.sessions.resource.v1.SessionResource;
import com.rebrowse.session.utils.AbstractSessionResourceTest;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.shared.sql.SQLContext;
import com.rebrowse.test.rest.data.UserAgentData;
import com.rebrowse.test.testconainers.api.auth.AuthApiTestResource;
import com.rebrowse.test.utils.RestAssuredUtils;
import com.rebrowse.api.query.TermCondition;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.session.sessions.datasource.sql.SqlSessionTable;
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

  private PageVisitCreateParams withUpdateOrganizationId(
      PageVisitCreateParams pageVisitCreateParams, String organizationId) {
    return new PageVisitCreateParams(
        organizationId,
        pageVisitCreateParams.getDeviceId(),
        pageVisitCreateParams.getHref(),
        pageVisitCreateParams.getReferrer(),
        pageVisitCreateParams.getDoctype(),
        pageVisitCreateParams.getScreenWidth(),
        pageVisitCreateParams.getScreenHeight(),
        pageVisitCreateParams.getWidth(),
        pageVisitCreateParams.getHeight(),
        pageVisitCreateParams.getCompiledTs());
  }

  private PageVisitCreateParams readSimplePage() throws URISyntaxException, IOException {
    return objectMapper.readValue(
        Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI())),
        PageVisitCreateParams.class);
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
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"doctype\":\"may not be null\",\"organizationId\":\"Required\",\"href\":\"may not be null\"}}}"));
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
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
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    Organization organization = authorizationFlows().retrieveUserData(sessionId).getOrganization();
    PageVisitCreateParams withFreePlanOrganization =
        withUpdateOrganizationId(readSimplePage(), organization.getId());
    String body = objectMapper.writeValueAsString(withFreePlanOrganization);

    Function<Integer, ValidatableResponse> createPage =
        (statusCode) ->
            given()
                .when()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.USER_AGENT, UserAgentData.MAC__SAFARI)
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
    PageVisitCreateParams withRandomOrganization =
        withUpdateOrganizationId(
            readSimplePage(), com.rebrowse.auth.organization.model.Organization.identifier());

    given()
        .when()
        .contentType(ContentType.JSON)
        .header(HttpHeaders.USER_AGENT, UserAgentData.MAC__SAFARI)
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
            .header(HttpHeaders.USER_AGENT, UserAgentData.MAC__SAFARI)
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
    String ssoSessionId = authorizationFlows().loginWithAdminUser();

    // GET newly created page
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, ssoSessionId)
        .get(String.format("%s/%s", PageVisitResource.PATH, pageId))
        .then()
        .statusCode(200)
        .body("data.organizationId", is(REBROWSE_ORGANIZATION_ID))
        .body("data.sessionId", is(sessionId.toString()));

    // GET newly created session
    DataResponse<SessionDTO> sessionDataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, ssoSessionId)
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
        .cookie(SsoSession.COOKIE_NAME, ssoSessionId)
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
            .header(HttpHeaders.USER_AGENT, UserAgentData.MAC__SAFARI)
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
        .cookie(SsoSession.COOKIE_NAME, ssoSessionId)
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
            .header(HttpHeaders.USER_AGENT, UserAgentData.MAC__SAFARI)
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
            .header(HttpHeaders.USER_AGENT, UserAgentData.MAC__SAFARI)
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
    String ssoSessionId = authorizationFlows().loginWithAdminUser();

    DataResponse<SessionDTO> firstSessionDataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, ssoSessionId)
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
            .cookie(SsoSession.COOKIE_NAME, ssoSessionId)
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

    assertEquals(MAC__SAFARI, sessions.getData().get(0).getUserAgent());
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
            .insertInto(SqlSessionTable.TABLE)
            .columns(SqlSessionTable.FIELDS)
            .values(
                sessionId,
                deviceId,
                organizationId,
                JsonObject.mapFrom(BOYDTON_US_VIRGINIA_NA),
                JsonObject.mapFrom(MAC__SAFARI),
                OffsetDateTime.ofInstant(
                    Instant.now().minus(31, ChronoUnit.MINUTES), ZoneOffset.UTC))
            .returning(SqlSessionTable.FIELDS);

    sqlPool.execute(query).toCompletableFuture().join();

    // page and session are created
    DataResponse<PageVisitSessionLink> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.USER_AGENT, UserAgentData.MAC__SAFARI)
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
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, PAGES_COUNT_PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, PAGES_COUNT_PATH);
  }

  @Test
  public void get_v1_pages_count__should_throw__when_unsupported_fields() {
    String sessionId = authorizationFlows().loginWithAdminUser();

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
                .get(PAGES_COUNT_PATH)
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
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(PAGES_COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":0}}"));
  }

  @Test
  public void get_v1_pages_count__should_count__when_group_by_referrer()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String organizationId =
        authorizationFlows().retrieveUserData(sessionId).getOrganization().getId();
    createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam(caseProvider.apply(GROUP_BY_PARAM), PageVisitTable.REFERRER)
                .get(PAGES_COUNT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson(
                        "{\"data\":[{\"count\":1,\"referrer\":\"https://facebook.com/\"},{\"count\":1,\"referrer\":\"https://github.com/\"},{\"count\":2,\"referrer\":\"https://google.com/\"},{\"count\":1,\"referrer\":\"https://instagram.com/\"}]}")));
  }

  @Test
  public void get_v1_pages_count__should_count__when_group_by_origin()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String organizationId =
        authorizationFlows().retrieveUserData(sessionId).getOrganization().getId();
    createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam(caseProvider.apply(GROUP_BY_PARAM), PageVisitTable.ORIGIN)
                .get(PAGES_COUNT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson(
                        "{\"data\":[{\"count\":3,\"origin\":\"https://localhost:3000\"},{\"count\":2,\"origin\":\"https://rebrowse.dev\"}]}")));
  }

  @Test
  public void get_v1_pages_count__should_count__when_group_by_path()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String organizationId =
        authorizationFlows().retrieveUserData(sessionId).getOrganization().getId();
    createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam(caseProvider.apply(GROUP_BY_PARAM), PageVisitTable.PATH)
                .get(PAGES_COUNT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson(
                        "{\"data\":[{\"count\":2,\"path\":\"\"},{\"count\":2,\"path\":\"/sessions\"},{\"count\":1,\"path\":\"/settings\"}]}")));
  }
}
