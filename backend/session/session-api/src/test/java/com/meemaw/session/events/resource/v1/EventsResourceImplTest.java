package com.meemaw.session.events.resource.v1;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.events.index.UserEventIndex;
import com.meemaw.events.model.internal.BrowserUnloadEvent;
import com.meemaw.events.model.internal.UserEvent;
import com.meemaw.session.resource.v1.SessionResource;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestExtension;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

  @BeforeAll
  public static void init() throws IOException {
    ElasticsearchTestExtension.getInstance()
        .restHighLevelClient()
        .indices()
        .create(
            new CreateIndexRequest(UserEventIndex.NAME).mapping(UserEventIndex.MAPPING),
            RequestOptions.DEFAULT);
  }

  @Test
  public void events_search_should_throw_when_unauthenticated() {
    String path = String.format(SEARCH_EVENTS_PATH_TEMPLATE, UUID.randomUUID());
    SsoTestSetupUtils.cookieExpect401(path, null);
    SsoTestSetupUtils.cookieExpect401(path, "random");
    SsoTestSetupUtils.cookieExpect401(path, SsoSession.newIdentifier());
  }

  @Test
  public void events_search_should_return_empty_list_on_random_session() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, SsoTestSetupUtils.login())
        .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, UUID.randomUUID()))
        .then()
        .statusCode(200)
        .body("data.size()", is(0));
  }

  @Test
  public void events_search_should_return_matching_events() throws IOException, URISyntaxException {
    String payload =
        Files.readString(
            Path.of(getClass().getResource("/events/browserUnloadEvent.json").toURI()));

    UUID pageId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID deviceId = UUID.randomUUID();

    UserEvent<BrowserUnloadEvent> userEvent =
        new UserEvent<>(
            JacksonMapper.get().readValue(payload, BrowserUnloadEvent.class),
            pageId,
            sessionId,
            deviceId,
            "000000");

    ElasticsearchTestExtension.getInstance()
        .restHighLevelClient()
        .index(
            new IndexRequest(UserEventIndex.NAME)
                .id(UUID.randomUUID().toString())
                .source(userEvent.index()),
            RequestOptions.DEFAULT);

    await()
        .atMost(30, TimeUnit.SECONDS)
        .untilAsserted(
            () ->
                given()
                    .when()
                    .cookie(SsoSession.COOKIE_NAME, SsoTestSetupUtils.login())
                    .get(String.format(SEARCH_EVENTS_PATH_TEMPLATE, sessionId))
                    .then()
                    .statusCode(200)
                    .body("data.size()", is(1)));
  }
}
