package com.meemaw.session.events.resource.v1;

import static com.meemaw.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;
import static com.meemaw.shared.rest.query.AbstractQueryParser.GROUP_BY_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.LIMIT_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.SORT_BY_PARAM;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.RestAssuredUtils.ssoBearerTokenTestCases;
import static com.meemaw.test.setup.RestAssuredUtils.ssoSessionCookieTestCases;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.events.index.UserEventIndex;
import com.meemaw.events.model.incoming.AbstractBrowserEvent;
import com.meemaw.events.model.incoming.UserEvent;
import com.meemaw.session.events.datasource.EventTable;
import com.meemaw.session.sessions.resource.v1.SessionResource;
import com.meemaw.shared.elasticsearch.ElasticsearchUtils;
import com.meemaw.test.rest.data.EventTestData;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.ExternalAuthApiProvidedTest;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestResource;
import com.rebrowse.api.query.TermCondition;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Method;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(ElasticsearchTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
@QuarkusTest
@Tag("integration")
public class EventsResourceImplTest extends ExternalAuthApiProvidedTest {

  private static final String SEARCH_EVENTS_PATH_TEMPLATE =
      String.join("/", SessionResource.PATH, "%s/events/search");

  private static final UUID SESSION_ID = UUID.randomUUID();
  private static final UUID PAGE_VISIT_ID = UUID.randomUUID();
  private static final UUID DEVICE_ID = UUID.randomUUID();

  @SuppressWarnings("rawtypes")
  private static Collection<AbstractBrowserEvent> loadIncomingEvents()
      throws URISyntaxException, IOException {
    return EventTestData.readIncomingEvents().stream()
        .map(
            eventPayload -> {
              try {
                return JacksonMapper.get().readValue(eventPayload, AbstractBrowserEvent.class);
              } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
              }
            })
        .collect(Collectors.toList());
  }

  @BeforeAll
  public static void init() throws IOException, URISyntaxException {
    loadIncomingEvents()
        .forEach(
            browserEvent -> {
              try {
                ElasticsearchUtils.restClient()
                    .index(
                        new IndexRequest(UserEventIndex.NAME)
                            .id(UUID.randomUUID().toString())
                            .source(
                                new UserEvent<>(
                                        browserEvent,
                                        PAGE_VISIT_ID,
                                        SESSION_ID,
                                        DEVICE_ID,
                                        REBROWSE_ORGANIZATION_ID)
                                    .index()),
                        RequestOptions.DEFAULT);
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            });
  }

  @Test
  public void events_search__should_throw__when_unauthorized() {
    String path = String.format(SEARCH_EVENTS_PATH_TEMPLATE, UUID.randomUUID());
    ssoSessionCookieTestCases(Method.GET, path);
    ssoBearerTokenTestCases(Method.GET, path);
  }

  @Test
  public void events_search__should_return_empty_list__on_random_session() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(200)
        .body("data.size()", is(0));
  }

  @Test
  public void events_search__should_throw__on_unsupported_fields() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .queryParam("random", TermCondition.GTE.rhs("aba"))
        .queryParam("aba", "gtecaba")
        .queryParam(GROUP_BY_PARAM, "another")
        .queryParam(SORT_BY_PARAM, "hehe")
        .queryParam(LIMIT_PARAM, "not_string")
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID))
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field\",\"random\":\"Unexpected field\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field\"},\"sort_by\":{\"hehe\":\"Unexpected field\"}}}}"));
  }

  @Test
  public void events_search__should_return_all_events__on_big_limit()
      throws IOException, URISyntaxException {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(LIMIT_PARAM, 100)
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID))
        .then()
        .statusCode(200)
        .body("data.size()", is(loadIncomingEvents().size()));

    String apiKey = authApi().createApiKey(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .queryParam(LIMIT_PARAM, 100)
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID))
        .then()
        .statusCode(200)
        .body("data.size()", is(loadIncomingEvents().size()));
  }

  @Test
  public void events_search__should_return_matching_events__when_type_filter() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(EventTable.TYPE, TermCondition.EQ.rhs(4))
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID))
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"clientX\":1167,\"clientY\":732,\"node\":{\":class\":\"__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw\",\":type\":\"submit\",\":data-baseweb\":\"button\",\"type\":\"<BUTTON\"},\"t\":1306,\"e\":4}]}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + "?event.e=eq:100")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }

  @Test
  public void events_search__should_return_matching_events__when_timestamp_filter() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(EventTable.TIMESTAMP, TermCondition.LT.rhs(1250))
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID))
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"location\":\"http://localhost:8080\",\"title\":\"Test title\",\"t\":1234,\"e\":0},{\"location\":\"http://localhost:8080\",\"t\":1234,\"e\":1},{\"innerWidth\":551,\"innerHeight\":232,\"t\":1234,\"e\":2},{\"location\":\"http://localhost:8080\",\"t\":1234,\"e\":8},{\"name\":\"http://localhost:3002/\",\"entryType\":\"navigation\",\"startTime\":0.0,\"duration\":5478.304999996908,\"t\":17,\"e\":3}]}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + "?event.t=lt:5")
        .then()
        .statusCode(200)
        .body("data.size()", is(0));
  }

  @Test
  public void events_search__should_return_matching_events__when_complex_type_filter() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam(EventTable.TYPE, TermCondition.GTE.rhs(11))
        .queryParam(EventTable.TYPE, TermCondition.LTE.rhs(12))
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID))
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"method\":\"GET\",\"url\":\"http://localhost:8082/v1/sessions\",\"status\":200,\"initiatorType\":\"xmlhttprequest\",\"nextHopProtocol\":\"h2\",\"t\":10812,\"e\":11},{\"method\":\"GET\",\"url\":\"http://localhost:8082/v1/sessions\",\"status\":200,\"type\":\"cors\",\"initiatorType\":\"fetch\",\"nextHopProtocol\":\"http/1.1\",\"t\":10812,\"e\":11},{\"name\":\"http://localhost:8082/v1/sessions\",\"startTime\":20.0,\"duration\":40.0,\"initiatorType\":\"fetch\",\"nextHopProtocol\":\"http/1.1\",\"t\":10812,\"e\":12}]}"));
  }
}
