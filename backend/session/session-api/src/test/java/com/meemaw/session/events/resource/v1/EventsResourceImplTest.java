package com.meemaw.session.events.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.INSIGHT_ORGANIZATION_ID;
import static com.meemaw.test.setup.SsoTestSetupUtils.cookieExpect401;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdmin;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.events.index.UserEventIndex;
import com.meemaw.events.model.incoming.AbstractBrowserEvent;
import com.meemaw.events.model.incoming.UserEvent;
import com.meemaw.session.sessions.v1.SessionResource;
import com.meemaw.shared.elasticsearch.ElasticsearchUtils;
import com.meemaw.test.rest.data.EventTestData;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(ElasticsearchTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
@QuarkusTest
@Tag("integration")
public class EventsResourceImplTest {

  private static final String SEARCH_EVENTS_PATH_TEMPLATE =
      String.join("/", SessionResource.PATH, "%s/events/search");

  private static final UUID SESSION_ID = UUID.randomUUID();
  private static final UUID PAGE_ID = UUID.randomUUID();
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
                                        PAGE_ID,
                                        SESSION_ID,
                                        DEVICE_ID,
                                        INSIGHT_ORGANIZATION_ID)
                                    .index()),
                        RequestOptions.DEFAULT);
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            });
  }

  @Test
  public void events_search_should_throw_when_unauthenticated() {
    String path = String.format(SEARCH_EVENTS_PATH_TEMPLATE, UUID.randomUUID());
    cookieExpect401(path, null);
    cookieExpect401(path, "random");
    cookieExpect401(path, SsoSession.newIdentifier());
  }

  @Test
  public void events_search_should_return_empty_list_on_random_session() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(200)
        .body("data.size()", is(0));
  }

  @Test
  public void events_search__should_throw__on_unsupported_fields() {
    String path =
        String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID)
            + "?random=gte:aba&aba=gtecaba&group_by=another&sort_by=hehe&limit=not_string";

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field in search query\",\"random\":\"Unexpected field in search query\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field in group_by query\"},\"sort_by\":{\"ehe\":\"Unexpected field in sort_by query\"}}}}"));
  }

  @Test
  public void events_search_should_return_all_events() throws IOException, URISyntaxException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + "?limit=100")
        .then()
        .statusCode(200)
        .body("data.size()", is(loadIncomingEvents().size()));
  }

  @Test
  public void events_search_should_return_event_type_matching_events() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + "?event.e=eq:4")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"clientX\":1167,\"clientY\":732,\"node\":{\":class\":\"__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw\",\":type\":\"submit\",\":data-baseweb\":\"button\",\"type\":\"<BUTTON\"},\"t\":1306,\"e\":4}]}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + "?event.e=eq:100")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }

  @Test
  public void events_search_should_return_event_timestamp_matching_events() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + "?event.t=lt:1250")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"location\":\"http://localhost:8080\",\"title\":\"Test title\",\"t\":1234,\"e\":0},{\"location\":\"http://localhost:8080\",\"t\":1234,\"e\":1},{\"innerWidth\":551,\"innerHeight\":232,\"t\":1234,\"e\":2},{\"location\":\"http://localhost:8080\",\"t\":1234,\"e\":8},{\"name\":\"http://localhost:3002/\",\"entryType\":\"navigation\",\"startTime\":0.0,\"duration\":5478.304999996908,\"t\":17,\"e\":3}]}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + "?event.t=lt:5")
        .then()
        .statusCode(200)
        .body("data.size()", is(0));
  }

  @Test
  public void events_search_should_search_by_type_and_clause_query() {
    String searchQuery = "?event.e=gte:11&event.e=lte:12";
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + searchQuery)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"method\":\"GET\",\"url\":\"http://localhost:8082/v1/sessions\",\"status\":200,\"initiatorType\":\"xmlhttprequest\",\"nextHopProtocol\":\"h2\",\"t\":10812,\"e\":11},{\"method\":\"GET\",\"url\":\"http://localhost:8082/v1/sessions\",\"status\":200,\"type\":\"cors\",\"initiatorType\":\"fetch\",\"nextHopProtocol\":\"http/1.1\",\"t\":10812,\"e\":11},{\"name\":\"http://localhost:8082/v1/sessions\",\"startTime\":20.0,\"duration\":40.0,\"initiatorType\":\"fetch\",\"nextHopProtocol\":\"http/1.1\",\"t\":10812,\"e\":12}]}"));
  }
}
