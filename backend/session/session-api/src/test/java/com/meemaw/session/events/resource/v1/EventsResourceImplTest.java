package com.meemaw.session.events.resource.v1;

import static com.meemaw.test.setup.SsoTestSetupUtils.cookieExpect401;
import static com.meemaw.test.setup.SsoTestSetupUtils.login;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.events.index.UserEventIndex;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import com.meemaw.events.model.internal.UserEvent;
import com.meemaw.session.resource.v1.SessionResource;
import com.meemaw.test.matchers.SameJSON;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestExtension;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
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

  // TODO: read whole directory
  private static Collection<AbstractBrowserEvent> loadEvents() {
    return List.of("unloadEvent.json", "performanceEvent.json", "clickEvent.json", "logEvent.json")
        .stream()
        .map(
            eventFileName -> {
              String path = String.format("/events/%s", eventFileName);
              try {
                String payload =
                    Files.readString(
                        Path.of(EventsResourceImplTest.class.getResource(path).toURI()));

                return JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
              } catch (IOException | URISyntaxException ex) {
                throw new RuntimeException(ex);
              }
            })
        .collect(Collectors.toList());
  }

  @BeforeAll
  public static void init() throws IOException {
    ElasticsearchTestExtension.getInstance()
        .restHighLevelClient()
        .indices()
        .create(
            new CreateIndexRequest(UserEventIndex.NAME).mapping(UserEventIndex.MAPPING),
            RequestOptions.DEFAULT);

    loadEvents()
        .forEach(
            browserEvent -> {
              try {
                ElasticsearchTestExtension.getInstance()
                    .restHighLevelClient()
                    .index(
                        new IndexRequest(UserEventIndex.NAME)
                            .id(UUID.randomUUID().toString())
                            .source(
                                new UserEvent<>(
                                        browserEvent, PAGE_ID, SESSION_ID, DEVICE_ID, "000000")
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
        .cookie(SsoSession.COOKIE_NAME, login())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(200)
        .body("data.size()", is(0));
  }

  // TODO: smarter comparison than raw string
  @Test
  public void events_search_should_return_all_events() {
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () ->
                given()
                    .when()
                    .cookie(SsoSession.COOKIE_NAME, login())
                    .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID))
                    .then()
                    .statusCode(200)
                    .body(
                        SameJSON.sameJson(
                            "{\"data\":[{\"name\":\"http://localhost:8081/v1/page\",\"entryType\":\"resource\",\"startTime\":3963.6150000151247,\"duration\":29.37000000383705,\"t\":34,\"e\":\"3\"},{\"location\":\"http://localhost:8080\",\"t\":1234,\"e\":\"1\"},{\"clientX\":1167,\"clientY\":732,\"node\":{\":class\":\"__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw\",\":type\":\"submit\",\":data-baseweb\":\"button\",\"type\":\"<BUTTON\"},\"t\":1306,\"e\":\"4\"},{\"level\":\"error\",\"arguments\":[\"HAHA\"],\"t\":10812,\"e\":\"9\"}]}")));
  }

  @Test
  public void events_search_should_return_matching_events() {
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () ->
                given()
                    .when()
                    .cookie(SsoSession.COOKIE_NAME, login())
                    .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, SESSION_ID) + "?event.e=4")
                    .then()
                    .statusCode(200)
                    .body(
                        SameJSON.sameJson(
                            "{\"data\":[{\"clientX\":1167,\"clientY\":732,\"node\":{\":class\":\"__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw\",\":type\":\"submit\",\":data-baseweb\":\"button\",\"type\":\"<BUTTON\"},\"t\":1306,\"e\":\"4\"}]}")));
  }
}
