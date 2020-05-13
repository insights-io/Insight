package com.meemaw.rec.resource.v1.beacon;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.events.model.external.UserEvent;
import com.meemaw.events.stream.EventsStream;
import com.meemaw.rec.beacon.resource.v1.BeaconResource;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.session.resource.v1.SessionResource;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.testconainers.api.session.SessionApiTestResource;
import com.meemaw.test.testconainers.kafka.KafkaTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.GenericType;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTestResource(SessionApiTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@QuarkusTest
@Tag("integration")
public class BeaconBeatResourceProcessingTest {

  @Inject @RestClient SessionResource sessionResource;

  private static final String ORG_ID = Organization.identifier();

  private static List<UserEvent<?>> events;
  private static List<UserEvent<?>> unloadEvents;

  @Incoming(EventsStream.ALL)
  public void process(UserEvent<?> event) {
    events.add(event);
  }

  @Incoming(EventsStream.UNLOAD)
  public void processUnloadEvent(UserEvent<?> event) {
    unloadEvents.add(event);
  }

  @BeforeEach
  public void init() {
    events = new ArrayList<>();
    unloadEvents = new ArrayList<>();
  }

  protected Uni<PageIdentity> insertPage(UUID uid) {
    CreatePageDTO payload =
        new CreatePageDTO(
            ORG_ID, uid, "testURL", "testReferrer", "testDocType", 200, 200, 200, 200, 200);

    return Uni.createFrom()
        .completionStage(
            sessionResource
                .page(payload)
                .thenApply(
                    response -> {
                      DataResponse<PageIdentity> dataResponse =
                          response.readEntity(new GenericType<>() {});
                      return dataResponse.getData();
                    }));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void shouldProcessLargeBeacon(String contentType) throws IOException, URISyntaxException {
    UUID uid = UUID.randomUUID();
    PageIdentity pageIdentity = insertPage(uid).await().indefinitely();
    UUID sessionID = pageIdentity.getSessionId();
    UUID pageID = pageIdentity.getPageId();

    String payload =
        Files.readString(Path.of(getClass().getResource("/beacon/initial.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam("SessionID", sessionID)
        .queryParam("UserID", uid)
        .queryParam("PageID", pageID)
        .queryParam("OrgID", ORG_ID)
        .body(payload)
        .post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(204);

    assertEquals(382, events.size());
    assertEquals(0, unloadEvents.size());
    events.forEach(
        e -> {
          assertEquals(pageID, e.getPageId());
          assertEquals(sessionID, e.getSessionID());
          assertEquals(uid, e.getUid());
          assertEquals(ORG_ID, e.getOrgID());
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void shouldProcessMultipleSmallBeacons(String contentType)
      throws IOException, URISyntaxException {
    UUID uid = UUID.randomUUID();
    PageIdentity pageIdentity = insertPage(uid).await().indefinitely();
    UUID sessionID = pageIdentity.getSessionId();
    UUID pageID = pageIdentity.getPageId();

    String payload =
        Files.readString(Path.of(getClass().getResource("/beacon/small.json").toURI()));

    for (int i = 0; i < 100; i++) {
      given()
          .when()
          .contentType(contentType)
          .queryParam("SessionID", sessionID)
          .queryParam("UserID", uid)
          .queryParam("PageID", pageID)
          .queryParam("OrgID", ORG_ID)
          .body(payload)
          .post(BeaconResource.PATH + "/beat")
          .then()
          .statusCode(204);

      assertEquals(i + 1, events.size());
      assertEquals(pageID, events.get(i).getPageId());
      assertEquals(sessionID, events.get(i).getSessionID());
      assertEquals(ORG_ID, events.get(i).getOrgID());
      assertEquals(uid, events.get(i).getUid());
    }

    assertEquals(0, unloadEvents.size());
  }

  private static final String GET_PAGE_END_RAW_SQL = "SELECT page_end FROM rec.page WHERE id = $1;";

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void shouldEndPageOnUnloadEvent(String contentType)
      throws IOException, URISyntaxException {
    UUID uid = UUID.randomUUID();
    PageIdentity pageIdentity = insertPage(uid).await().indefinitely();
    UUID sessionID = pageIdentity.getSessionId();
    UUID pageID = pageIdentity.getPageId();

    String payload =
        Files.readString(Path.of(getClass().getResource("/beacon/withUnloadEvent.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam("SessionID", sessionID)
        .queryParam("UserID", uid)
        .queryParam("PageID", pageID)
        .queryParam("OrgID", ORG_ID)
        .body(payload)
        .post(BeaconResource.PATH + "/beat")
        .then()
        .statusCode(204);

    assertEquals(2, events.size());
    assertEquals(1, unloadEvents.size());
    events.forEach(
        e -> {
          assertEquals(pageID, e.getPageId());
          assertEquals(sessionID, e.getSessionID());
          assertEquals(uid, e.getUid());
          assertEquals(ORG_ID, e.getOrgID());
        });
    unloadEvents.forEach(
        e -> {
          assertEquals(pageID, e.getPageId());
          assertEquals(sessionID, e.getSessionID());
          assertEquals(uid, e.getUid());
          assertEquals(ORG_ID, e.getOrgID());
        });
  }
}
