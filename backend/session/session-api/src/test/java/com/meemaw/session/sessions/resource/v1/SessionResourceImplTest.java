package com.meemaw.session.sessions.resource.v1;

import static com.meemaw.shared.rest.query.AbstractQueryParser.DATA_TRUNC_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.GROUP_BY_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.LIMIT_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.SORT_BY_PARAM;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.RestAssuredUtils.ssoBearerTokenTestCases;
import static com.meemaw.test.setup.RestAssuredUtils.ssoSessionCookieTestCases;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.location.model.Located;
import com.meemaw.session.location.service.LocationService;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionTable;
import com.meemaw.shared.rest.query.TimePrecision;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.api.query.TermCondition;
import io.quarkus.test.Mock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Method;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
public class SessionResourceImplTest extends AbstractSessionResourceTest {

  @Test
  public void get_v1_sessions__should_throw__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, SessionResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, SessionResource.PATH);
  }

  @Test
  public void get__v1_sessions__should_throw__on_unsupported_fields() {
    given()
        .when()
        .queryParam("random", TermCondition.GTE.rhs("aba"))
        .queryParam("aba", "gtecaba")
        .queryParam(GROUP_BY_PARAM, "another")
        .queryParam(SORT_BY_PARAM, "hehe")
        .queryParam(LIMIT_PARAM, "not_string")
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .get(SessionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field\",\"random\":\"Unexpected field\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field\"},\"sort_by\":{\"hehe\":\"Unexpected field\"}}}}"));
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
        .queryParam("random", TermCondition.GTE.rhs("aba"))
        .queryParam("aba", "gtecaba")
        .queryParam(GROUP_BY_PARAM, "another")
        .queryParam(SORT_BY_PARAM, "hehe")
        .queryParam(LIMIT_PARAM, "not_string")
        .get(COUNT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field\",\"random\":\"Unexpected field\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field\"},\"sort_by\":{\"hehe\":\"Unexpected field\"}}}}"));
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
        .queryParam(SessionTable.CREATED_AT, TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":5}}"));

    String apiKey = authApi().createApiKey(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .queryParam(SessionTable.CREATED_AT, TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
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
        .queryParam(SessionTable.CREATED_AT, TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
        .queryParam(SessionTable.LOCATION__CITY, TermCondition.EQ.rhs("Maribor"))
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

    CASE_PROVIDERS.forEach(
        caseProvider ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam(
                    caseProvider.apply(GROUP_BY_PARAM),
                    caseProvider.apply(SessionTable.LOCATION__COUNTRY))
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
                .get(COUNT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson(
                        String.format(
                            "{\"data\":[{\"count\":1,\"%s\":\"Canada\"},{\"count\":1,\"%s\":\"Croatia\"},{\"count\":2,\"%s\":\"Slovenia\"},{\"count\":1,\"%s\":\"United States\"}]}",
                            caseProvider.apply(SessionTable.LOCATION__COUNTRY),
                            caseProvider.apply(SessionTable.LOCATION__COUNTRY),
                            caseProvider.apply(SessionTable.LOCATION__COUNTRY),
                            caseProvider.apply(SessionTable.LOCATION__COUNTRY)))));
  }

  @Test
  public void get_v1_session_count__should_return_counts__on_group_by_country_and_continent()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam(
                    caseProvider.apply(GROUP_BY_PARAM),
                    caseProvider.apply(SessionTable.LOCATION__COUNTRY),
                    caseProvider.apply(SessionTable.LOCATION__CONTINENT))
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
                .get(COUNT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson(
                        String.format(
                            "{\"data\":[{\"count\":1,\"%s\":\"Canada\",\"%s\":\"North America\"},{\"count\":1,\"%s\":\"Croatia\",\"%s\":\"Europe\"},{\"count\":2,\"%s\":\"Slovenia\",\"%s\":\"Europe\"},{\"count\":1,\"%s\":\"United States\",\"%s\":\"North America\"}]}",
                            caseProvider.apply(SessionTable.LOCATION__COUNTRY),
                            caseProvider.apply(SessionTable.LOCATION__CONTINENT),
                            caseProvider.apply(SessionTable.LOCATION__COUNTRY),
                            caseProvider.apply(SessionTable.LOCATION__CONTINENT),
                            caseProvider.apply(SessionTable.LOCATION__COUNTRY),
                            caseProvider.apply(SessionTable.LOCATION__CONTINENT),
                            caseProvider.apply(SessionTable.LOCATION__COUNTRY),
                            caseProvider.apply(SessionTable.LOCATION__CONTINENT)))));
  }

  @Test
  public void get_v1_session_count__should_return_counts__on_group_by_device()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam(
                    caseProvider.apply(GROUP_BY_PARAM),
                    caseProvider.apply(SessionTable.USER_AGENT__DEVICE_CLASS))
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
                .get(COUNT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson(
                        String.format(
                            "{\"data\":[{\"count\":2,\"%s\":\"Desktop\"},{\"count\":1,\"%s\":\"Phone\"},{\"count\":1,\"%s\":\"Set-top box\"},{\"count\":1,\"%s\":\"Tablet\"}]}",
                            caseProvider.apply(SessionTable.USER_AGENT__DEVICE_CLASS),
                            caseProvider.apply(SessionTable.USER_AGENT__DEVICE_CLASS),
                            caseProvider.apply(SessionTable.USER_AGENT__DEVICE_CLASS),
                            caseProvider.apply(SessionTable.USER_AGENT__DEVICE_CLASS)))));
  }

  @Test
  public void get_v1_session_count__should_return_counts__on_group_by_created_at_date_trunc_second()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    SessionDTO sessionFirstBatch = createTestSessions(organizationId).get(0);
    SessionDTO sessionSecondBatch = createTestSessions(organizationId).get(0);

    assertTrue(sessionSecondBatch.getCreatedAt().isAfter(sessionFirstBatch.getCreatedAt()));

    CASE_PROVIDERS.forEach(
        caseProvider ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam(
                    caseProvider.apply(GROUP_BY_PARAM), caseProvider.apply(SessionTable.CREATED_AT))
                .queryParam(
                    caseProvider.apply(DATA_TRUNC_PARAM), TimePrecision.MICROSECONDS.getKey())
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessionFirstBatch.getCreatedAt()))
                .get(COUNT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson(
                        String.format(
                            "{\"data\":[{\"count\":5,\"%s\":\"%s\"},{\"count\":5,\"%s\":\"%s\"}]}",
                            caseProvider.apply(SessionTable.CREATED_AT),
                            sessionFirstBatch
                                .getCreatedAt()
                                .format(RebrowseApi.DATE_TIME_FORMATTER),
                            caseProvider.apply(SessionTable.CREATED_AT),
                            sessionSecondBatch
                                .getCreatedAt()
                                .format(RebrowseApi.DATE_TIME_FORMATTER)))));
  }

  @Test
  public void get_v1_sessions_distinct__should_throw__on_unauthorized() {
    ssoSessionCookieTestCases(Method.GET, DISTINCT_PATH);
    ssoBearerTokenTestCases(Method.GET, DISTINCT_PATH);
  }

  @Test
  public void get_v1_sessions_distinct__should_throw__when_no_columns() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(DISTINCT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"on\":\"Required\"}}}"));
  }

  @Test
  public void get_v1_sessions_distinct__should_return_cities() throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("on", SessionTable.LOCATION__CITY)
        .queryParam(SessionTable.CREATED_AT, TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Maribor\",\"New York\",\"Otawa\",\"Zagreb\"]}"));

    String apiKey = authApi().createApiKey(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .queryParam("on", SessionTable.LOCATION__CITY)
        .queryParam(SessionTable.CREATED_AT, TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Maribor\",\"New York\",\"Otawa\",\"Zagreb\"]}"));
  }

  @Test
  public void get_v1_sessions_distinct__should_return_continents() throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam("on", caseProvider.apply(SessionTable.LOCATION__CONTINENT))
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
                .get(DISTINCT_PATH)
                .then()
                .statusCode(200)
                .body(sameJson("{\"data\":[\"Europe\",\"North America\"]}")));
  }

  @Test
  public void get_v1_sessions_distinct__should_return_countries() throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam("on", caseProvider.apply(SessionTable.LOCATION__COUNTRY))
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
                .get(DISTINCT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson(
                        "{\"data\":[\"Canada\",\"Croatia\",\"Slovenia\",\"United States\"]}")));
  }

  @Test
  public void get_v1_sessions_distinct__should_return_regions() throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam("on", caseProvider.apply(SessionTable.LOCATION__REGION))
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
                .get(DISTINCT_PATH)
                .then()
                .statusCode(200)
                .body(sameJson("{\"data\":[\"Podravska\",\"Washington\"]}")));
  }

  @Test
  public void get_v1_sessions_distinct__should_return_agent_name() throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) -> {
          given()
              .when()
              .cookie(SsoSession.COOKIE_NAME, sessionId)
              .queryParam("on", caseProvider.apply(SessionTable.USER_AGENT__AGENT_NAME))
              .queryParam(
                  caseProvider.apply(SessionTable.CREATED_AT),
                  TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
              .get(DISTINCT_PATH)
              .then()
              .statusCode(200)
              .body(sameJson("{\"data\":[\"Chrome\",\"Chrome Webview\",\"Edge\",\"Safari\"]}"));
        });
  }

  @Test
  public void get_v1_sessions_distinct__should_return_operating_system_name()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam(
                    "on", caseProvider.apply(SessionTable.USER_AGENT__OPERATING_SYSTEM_NAME))
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
                .get(DISTINCT_PATH)
                .then()
                .statusCode(200)
                .body(
                    sameJson("{\"data\":[\"Android\",\"Mac OS X\",\"Unknown\",\"Windows NT\"]}"))));
  }

  @Test
  public void get_v1_sessions_insights_distinct__should_return_device_class()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().retrieveUserData(sessionId).getOrganization().getId();
    List<SessionDTO> sessions = createTestSessions(organizationId);

    CASE_PROVIDERS.forEach(
        (caseProvider) ->
            given()
                .when()
                .cookie(SsoSession.COOKIE_NAME, sessionId)
                .queryParam("on", caseProvider.apply(SessionTable.USER_AGENT__DEVICE_CLASS))
                .queryParam(
                    caseProvider.apply(SessionTable.CREATED_AT),
                    TermCondition.GTE.rhs(sessions.get(0).getCreatedAt()))
                .get(DISTINCT_PATH)
                .then()
                .statusCode(200)
                .body(sameJson("{\"data\":[\"Desktop\",\"Phone\",\"Set-top box\",\"Tablet\"]}")));
  }

  @Test
  public void get_v1_sessions_distinct__should_throw__when_unexpected_fields() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "random")
        .get(DISTINCT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"random\":\"Unexpected field\"}}}"));
  }

  @Mock
  @ApplicationScoped
  public static class MockedLocationService implements LocationService {

    @Override
    public Located lookupByIp(String ip) {
      return BOYDTON_US_VIRGINIA_NA;
    }
  }
}
