package com.meemaw.rec.resource.v1.beacon;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.meemaw.rec.beacon.resource.v1.BeaconResource;
import com.meemaw.shared.event.EventsChannel;
import com.meemaw.shared.event.model.AbstractBrowserEvent;
import com.meemaw.shared.event.model.AbstractBrowserEventBatch;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.test.testconainers.kafka.KafkaResource;
import com.meemaw.test.testconainers.pg.Postgres;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


@Postgres
@QuarkusTestResource(KafkaResource.class)
@QuarkusTest
@Tag("integration")
public class BeaconBeatResourceProcessingTest {

  private static List<AbstractBrowserEventBatch> events;
  private static List<AbstractBrowserEvent> unloadEvents;

  @Inject
  PgPool pgPool;

  @Incoming(EventsChannel.ALL)
  public void process(AbstractBrowserEventBatch batch) {
    events.add(batch);
  }

  @Incoming(EventsChannel.UNLOAD)
  public void processUnloadEvent(AbstractBrowserEvent event) {
    unloadEvents.add(event);
  }


  @BeforeEach
  public void init() {
    events = new ArrayList<>();
    unloadEvents = new ArrayList<>();
  }

  private static final String INSERT_PAGE_RAW_SQL = "INSERT INTO rec.page (id, uid, session_id, organization, doctype, url, referrer, height, width, screen_height, screen_width, compiled_timestamp) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12);";

  protected Uni<Void> insertPage(UUID pageId, UUID uid, UUID sessionId) {
    Tuple values = Tuple.newInstance(io.vertx.sqlclient.Tuple.of(
        pageId,
        uid,
        sessionId,
        "testOrganization",
        "testDocType",
        "testURL",
        "testReferrer",
        200,
        200,
        200,
        200,
        200
    ));

    return pgPool.preparedQuery(INSERT_PAGE_RAW_SQL, values)
        .onItem()
        .ignore()
        .andContinueWithNull()
        .onFailure().invoke(throwable -> {
          throw new DatabaseException();
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void shouldProcessLargeBeacon(String contentType)
      throws IOException, URISyntaxException {
    UUID sessionID = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    UUID pageID = UUID.randomUUID();

    insertPage(pageID, uid, sessionID).await().indefinitely();

    String payload = Files.readString(Path.of(getClass().getResource(
        "/beacon/initial.json").toURI()));

    given()
        .when().contentType(contentType)
        .queryParam("SessionID", sessionID)
        .queryParam("UserID", uid)
        .queryParam("PageID", pageID)
        .body(payload)
        .post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(204);

    assertEquals(382, events.get(0).getEvents().size());
    assertEquals(pageID, events.get(0).getPageId());
    assertEquals(0, unloadEvents.size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void shouldProcessMultipleSmallBeacons(String contentType)
      throws IOException, URISyntaxException {
    UUID sessionID = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    UUID pageID = UUID.randomUUID();

    insertPage(pageID, uid, sessionID).await().indefinitely();

    String payload = Files.readString(Path.of(getClass().getResource(
        "/beacon/small.json").toURI()));

    for (int i = 0; i < 100; i++) {
      given()
          .when()
          .contentType(contentType)
          .queryParam("SessionID", sessionID)
          .queryParam("UserID", uid)
          .queryParam("PageID", pageID)
          .body(payload)
          .post(BeaconResource.PATH + "/beat")
          .then()
          .statusCode(204);

      assertEquals(i + 1, events.size());
      assertEquals(1, events.get(i).getEvents().size());
      assertEquals(pageID, events.get(i).getPageId());
    }

    assertEquals(0, unloadEvents.size());
  }

  private static final String GET_PAGE_END_RAW_SQL = "SELECT page_end FROM rec.page WHERE id = $1;";

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void shouldEndPageOnUnloadEvent(String contentType)
      throws IOException, URISyntaxException {
    UUID sessionID = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    UUID pageID = UUID.randomUUID();

    insertPage(pageID, uid, sessionID).await().indefinitely();

    String payload = Files.readString(Path.of(getClass().getResource(
        "/beacon/withUnloadEvent.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam("SessionID", sessionID)
        .queryParam("UserID", uid)
        .queryParam("PageID", pageID)
        .body(payload)
        .post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(204);

    assertEquals(pageID, events.get(0).getPageId());
    assertEquals(1, events.size());
    assertEquals(2, events.get(0).getEvents().size());
    assertEquals(1, unloadEvents.size());

    Instant pageEnd = getPageEnd(pageID).await().indefinitely();
    // was in last second
    assertTrue(Math.abs(Duration.between(Instant.now(), pageEnd).toMillis()) < 1000);
  }

  private Uni<Instant> getPageEnd(UUID pageId) {
    return pgPool.preparedQuery(GET_PAGE_END_RAW_SQL, Tuple.of(pageId))
        .map(rowSet -> rowSet.iterator().next().getOffsetDateTime("page_end").toInstant());
  }
}
