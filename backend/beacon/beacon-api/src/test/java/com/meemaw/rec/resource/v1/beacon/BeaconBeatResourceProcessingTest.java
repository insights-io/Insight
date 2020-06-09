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

  private static final String BEACON_RESOURCE_BEAT_PATH = BeaconResource.PATH + "/beat";
  private static final String ORGANIZATION_ID = Organization.identifier();

  @Inject @RestClient SessionResource sessionResource;

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

  protected Uni<PageIdentity> insertPage(UUID deviceId) {
    CreatePageDTO payload =
        new CreatePageDTO(
            ORGANIZATION_ID,
            deviceId,
            "testURL",
            "testReferrer",
            "testDocType",
            200,
            200,
            200,
            200,
            200);

    return Uni.createFrom()
        .completionStage(
            sessionResource
                .createPage(payload)
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
    UUID deviceId = UUID.randomUUID();
    PageIdentity pageIdentity = insertPage(deviceId).await().indefinitely();
    UUID sessionID = pageIdentity.getSessionId();
    UUID pageID = pageIdentity.getPageId();
    String body = Files.readString(Path.of(getClass().getResource("/beacon/initial.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam("sessionId", sessionID)
        .queryParam("deviceId", deviceId)
        .queryParam("pageId", pageID)
        .queryParam("organizationId", ORGANIZATION_ID)
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(204);

    assertEquals(382, events.size());
    assertEquals(0, unloadEvents.size());
    events.forEach(
        e -> {
          assertEquals(pageID, e.getPageId());
          assertEquals(sessionID, e.getSessionId());
          assertEquals(deviceId, e.getDeviceId());
          assertEquals(ORGANIZATION_ID, e.getOrganizationId());
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void shouldProcessMultipleSmallBeacons(String contentType)
      throws IOException, URISyntaxException {
    UUID deviceId = UUID.randomUUID();
    PageIdentity pageIdentity = insertPage(deviceId).await().indefinitely();
    UUID sessionID = pageIdentity.getSessionId();
    UUID pageID = pageIdentity.getPageId();
    String body = Files.readString(Path.of(getClass().getResource("/beacon/small.json").toURI()));

    for (int i = 0; i < 100; i++) {
      given()
          .when()
          .contentType(contentType)
          .queryParam("sessionId", sessionID)
          .queryParam("deviceId", deviceId)
          .queryParam("pageId", pageID)
          .queryParam("organizationId", ORGANIZATION_ID)
          .body(body)
          .post(BEACON_RESOURCE_BEAT_PATH)
          .then()
          .statusCode(204);

      assertEquals(i + 1, events.size());
      assertEquals(pageID, events.get(i).getPageId());
      assertEquals(sessionID, events.get(i).getSessionId());
      assertEquals(ORGANIZATION_ID, events.get(i).getOrganizationId());
      assertEquals(deviceId, events.get(i).getDeviceId());
    }

    assertEquals(0, unloadEvents.size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "text/plain"})
  public void shouldEndPageOnUnloadEvent(String contentType)
      throws IOException, URISyntaxException {
    UUID deviceId = UUID.randomUUID();
    PageIdentity pageIdentity = insertPage(deviceId).await().indefinitely();
    UUID sessionID = pageIdentity.getSessionId();
    UUID pageID = pageIdentity.getPageId();

    String body =
        Files.readString(Path.of(getClass().getResource("/beacon/withUnloadEvent.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam("sessionId", sessionID)
        .queryParam("deviceId", deviceId)
        .queryParam("pageId", pageID)
        .queryParam("organizationId", ORGANIZATION_ID)
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(204);

    assertEquals(2, events.size());
    assertEquals(1, unloadEvents.size());
    events.forEach(
        e -> {
          assertEquals(pageID, e.getPageId());
          assertEquals(sessionID, e.getSessionId());
          assertEquals(deviceId, e.getDeviceId());
          assertEquals(ORGANIZATION_ID, e.getOrganizationId());
        });
    unloadEvents.forEach(
        e -> {
          assertEquals(pageID, e.getPageId());
          assertEquals(sessionID, e.getSessionId());
          assertEquals(deviceId, e.getDeviceId());
          assertEquals(ORGANIZATION_ID, e.getOrganizationId());
        });
  }
}
