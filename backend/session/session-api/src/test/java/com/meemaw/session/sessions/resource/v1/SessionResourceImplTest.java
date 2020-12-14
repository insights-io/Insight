package com.meemaw.session.sessions.resource.v1;

import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.FIELDS;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.TABLE;
import static com.meemaw.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;
import static com.meemaw.shared.rest.query.AbstractQueryParser.DATA_TRUNC_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.GROUP_BY_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.LIMIT_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.SORT_BY_PARAM;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.RestAssuredUtils.ssoBearerTokenTestCases;
import static com.meemaw.test.setup.RestAssuredUtils.ssoSessionCookieTestCases;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.location.model.Location;
import com.meemaw.session.location.service.LocationService;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionTable;
import com.meemaw.shared.rest.query.TimePrecision;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.shared.sql.SQLContext;
import com.meemaw.test.rest.data.UserAgentData;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.model.organization.Organization;
import io.quarkus.test.Mock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.ValidatableResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import org.jooq.Query;
import org.jooq.conf.ParamType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
public class SessionResourceImplTest extends AbstractSessionResourceTest {

  public static final String SESSION_PATH_TEMPLATE = String.join("/", SessionResource.PATH, "%s");
  public static final String SESSION_PAGE_PATH_TEMPLATE =
      String.join("/", SESSION_PATH_TEMPLATE, "pages", "%s");

  @Inject PgPool pgPool;

  private CreatePageDTO withUpdateOrganizationId(
      CreatePageDTO createPageDTO, String organizationId) {
    return new CreatePageDTO(
        organizationId,
        createPageDTO.getDeviceId(),
        createPageDTO.getUrl(),
        createPageDTO.getReferrer(),
        createPageDTO.getDoctype(),
        createPageDTO.getScreenWidth(),
        createPageDTO.getScreenHeight(),
        createPageDTO.getWidth(),
        createPageDTO.getHeight(),
        createPageDTO.getCompiledTs());
  }

  private CreatePageDTO readSimplePage() throws URISyntaxException, IOException {
    return objectMapper.readValue(
        Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI())),
        CreatePageDTO.class);
  }

  @Test
  public void post_page__should_throw__when_free_quota_exceeded()
      throws IOException, URISyntaxException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    Organization organization = authApi().retrieveOrganization(sessionId);
    CreatePageDTO withFreePlanOrganization =
        withUpdateOrganizationId(readSimplePage(), organization.getId());
    String body = objectMapper.writeValueAsString(withFreePlanOrganization);

    Function<Integer, ValidatableResponse> createPage =
        (statusCode) ->
            given()
                .when()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
                .body(body)
                .post(SessionResource.PATH)
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
  public void post_page__should_throw__when_missing_organization()
      throws IOException, URISyntaxException {
    CreatePageDTO withRandomOrganization =
        withUpdateOrganizationId(
            readSimplePage(), com.meemaw.auth.organization.model.Organization.identifier());

    given()
        .when()
        .contentType(ContentType.JSON)
        .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
        .body(objectMapper.writeValueAsString(withRandomOrganization))
        .post(SessionResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void post_page__should_link_sessions__when_matching_device_id()
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    // Create first session
    DataResponse<PageIdentity> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .body(payload)
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    PageIdentity pageIdentity = dataResponse.getData();
    UUID deviceId = pageIdentity.getDeviceId();
    UUID sessionId = pageIdentity.getSessionId();
    UUID pageId = pageIdentity.getPageId();

    // GET newly created page
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .get(String.format(SESSION_PAGE_PATH_TEMPLATE, sessionId, pageId))
        .then()
        .statusCode(200)
        .body("data.organizationId", is(REBROWSE_ORGANIZATION_ID))
        .body("data.sessionId", is(sessionId.toString()));

    // GET newly created session
    DataResponse<SessionDTO> sessionDataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
            .get(String.format(SESSION_PATH_TEMPLATE, sessionId))
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
        .get(
            String.format(
                "%s?created_at=gte:%s",
                SessionResource.PATH, sessionDataResponse.getData().getCreatedAt()))
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
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertNotEquals(dataResponse.getData().getPageId(), pageId);
    assertEquals(dataResponse.getData().getDeviceId(), deviceId);
    assertEquals(dataResponse.getData().getSessionId(), sessionId);

    // GET sessions again to confirm no new sessions has been created
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .get(
            String.format(
                "%s?created_at=gte:%s",
                SessionResource.PATH, sessionDataResponse.getData().getCreatedAt()))
        .then()
        .statusCode(200)
        .body("data.size()", is(1));
  }

  @Test
  public void post_page__should_create_new_session__when_no_matching_device_id()
      throws IOException, URISyntaxException {
    String payload = Files.readString(Path.of(getClass().getResource("/page/simple.json").toURI()));

    // page and session are created
    DataResponse<PageIdentity> dataResponse =
        given()
            .when()
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .contentType(ContentType.JSON)
            .body(payload)
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    PageIdentity pageIdentity = dataResponse.getData();
    UUID deviceId = pageIdentity.getDeviceId();
    UUID sessionId = pageIdentity.getSessionId();
    UUID pageId = pageIdentity.getPageId();

    // new page is created cause device id is not matching
    dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .body(payload)
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertNotEquals(dataResponse.getData().getDeviceId(), deviceId);
    assertNotEquals(dataResponse.getData().getSessionId(), sessionId);
    assertNotEquals(dataResponse.getData().getPageId(), pageId);

    DataResponse<SessionDTO> firstSessionDataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
            .get(String.format(SESSION_PATH_TEMPLATE, sessionId))
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
            .get(
                String.format(
                    "%s?created_at=gte:%s",
                    SessionResource.PATH, firstSessionDataResponse.getData().getCreatedAt()))
            .then()
            .statusCode(200)
            .body("data.size()", is(2))
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertEquals(MOCKED_USER_AGENT, sessions.getData().get(0).getUserAgent());
    assertEquals(MOCKED_LOCATION, sessions.getData().get(0).getLocation());
  }

  @Test
  public void post_page__should_create_new_session__when_no_session_timeout()
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
                JsonObject.mapFrom(MOCKED_LOCATION),
                JsonObject.mapFrom(MOCKED_USER_AGENT),
                OffsetDateTime.ofInstant(
                    Instant.now().minus(31, ChronoUnit.MINUTES), ZoneOffset.UTC))
            .returning(FIELDS);

    pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .await()
        .indefinitely();

    // page and session are created
    DataResponse<PageIdentity> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.USER_AGENT, UserAgentData.DESKTOP_MAC_CHROME)
            .body(payload)
            .post(SessionResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {});

    assertNotEquals(dataResponse.getData().getSessionId(), sessionId);
    assertEquals(dataResponse.getData().getDeviceId(), deviceId);
  }

  @Test
  public void get_v1_sessions_count__should_throw__on_unauthorized() {
    ssoSessionCookieTestCases(Method.GET, COUNT_PATH);
    ssoBearerTokenTestCases(Method.GET, COUNT_PATH);
  }

  @Test
  public void get_v1_sessions_count__should_throw__on_unsupported_fields() {
    String sessionId = authApi().loginWithAdminUser();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("random", "gte:aba")
        .queryParam("aba", "gtecaba")
        .queryParam(GROUP_BY_PARAM, "another")
        .queryParam(SORT_BY_PARAM, "hehe")
        .queryParam(LIMIT_PARAM, "not_string")
        .get(COUNT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field in search query\",\"random\":\"Unexpected field in search query\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field in group_by query\"},\"sort_by\":{\"hehe\":\"Unexpected field in sort_by query\"}}}}"));
  }

  @Test
  public void get_v1_sessions_count__should_return_count__on_empty_request()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(
            SessionTable.CREATED_AT, String.format("gte:%s", sessions.get(0).getCreatedAt()))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":5}}"));

    String apiKey = authApi().createApiKey(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .queryParam(
            SessionTable.CREATED_AT, String.format("gte:%s", sessions.get(0).getCreatedAt()))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":5}}"));
  }

  @Test
  public void get_v1_sessions_count__should_return_count__on_request_with_filters()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(
            SessionTable.CREATED_AT, String.format("gte:%s", sessions.get(0).getCreatedAt()))
        .queryParam(SessionTable.LOCATION__CITY, "eq:Maribor")
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":1}}"));
  }

  @Test
  public void get_v1_session_count__should_return_counts__on_group_by_country()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(GROUP_BY_PARAM, SessionTable.LOCATION__COUNTRY)
        .queryParam(
            SessionTable.CREATED_AT, String.format("gte:%s", sessions.get(0).getCreatedAt()))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"location.countryName\":\"Canada\"},{\"count\":1,\"location.countryName\":\"Croatia\"},{\"count\":2,\"location.countryName\":\"Slovenia\"},{\"count\":1,\"location.countryName\":\"United States\"}]}"));
  }

  @Test
  public void get_v1_session_count__should_return_counts__on_group_by_country_and_continent()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(
            GROUP_BY_PARAM,
            String.join(",", SessionTable.LOCATION__COUNTRY, SessionTable.LOCATION__CONTINENT))
        .queryParam(
            SessionTable.CREATED_AT, String.format("gte:%s", sessions.get(0).getCreatedAt()))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"location.countryName\":\"Canada\",\"location.continentName\":\"North America\"},{\"count\":1,\"location.countryName\":\"Croatia\",\"location.continentName\":\"Europe\"},{\"count\":2,\"location.countryName\":\"Slovenia\",\"location.continentName\":\"Europe\"},{\"count\":1,\"location.countryName\":\"United States\",\"location.continentName\":\"North America\"}]}"));
  }

  @Test
  public void get_v1_session_count__should_return_counts__on_group_by_device()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(GROUP_BY_PARAM, SessionTable.USER_AGENT__DEVICE_CLASS)
        .queryParam(
            SessionTable.CREATED_AT, String.format("gte:%s", sessions.get(0).getCreatedAt()))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"user_agent.deviceClass\":\"Desktop\"},{\"count\":4,\"user_agent.deviceClass\":\"Phone\"}]}"));
  }

  @Test
  public void get_v1_session_count__should_return_counts__on_group_by_created_at_date_trunc_second()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    SessionDTO sessionFirstBatch = createTestSessions(organizationId).get(0);
    SessionDTO sessionSecondBatch = createTestSessions(organizationId).get(0);

    assertTrue(sessionSecondBatch.getCreatedAt().isAfter(sessionFirstBatch.getCreatedAt()));
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(GROUP_BY_PARAM, SessionTable.CREATED_AT)
        .queryParam(DATA_TRUNC_PARAM, TimePrecision.MICROSECONDS.getKey())
        .queryParam(
            SessionTable.CREATED_AT, String.format("gte:%s", sessionFirstBatch.getCreatedAt()))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\":[{\"count\":5,\"created_at\":\"%s\"},{\"count\":5,\"created_at\":\"%s\"}]}",
                    sessionFirstBatch.getCreatedAt().format(RebrowseApi.DATE_TIME_FORMATTER),
                    sessionSecondBatch.getCreatedAt().format(RebrowseApi.DATE_TIME_FORMATTER))));
  }

  @Mock
  @ApplicationScoped
  public static class MockedLocationService implements LocationService {

    @Override
    public Location lookupByIp(String ip) {
      return MOCKED_LOCATION;
    }
  }
}
