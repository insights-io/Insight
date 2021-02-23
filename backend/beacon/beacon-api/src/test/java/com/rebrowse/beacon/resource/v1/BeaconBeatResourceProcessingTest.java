package com.rebrowse.beacon.resource.v1;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rebrowse.events.index.UserEventTable;
import com.rebrowse.events.model.incoming.UserEvent;
import com.rebrowse.events.stream.EventsStream;
import com.rebrowse.session.model.PageVisitCreateParams;
import com.rebrowse.session.model.PageVisitSessionLink;
import com.rebrowse.session.pages.resource.v1.PageVisitResource;
import com.rebrowse.shared.SharedConstants;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.test.rest.data.UserAgentData;
import com.rebrowse.test.testconainers.api.session.SessionApiTestResource;
import com.rebrowse.test.testconainers.kafka.KafkaTestResource;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.opentracing.Traced;
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

  // TODO: create new organization -- dont reuse
  private static final String ORGANIZATION_ID = SharedConstants.REBROWSE_ORGANIZATION_ID;
  private static final String BEACON_RESOURCE_BEAT_PATH = RecordingResource.PATH + "/beat";
  private static List<UserEvent<?>> events;
  private static List<UserEvent<?>> unloadEvents;

  @Inject @RestClient PageVisitResource pageVisitResource;

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

  @Traced
  protected Uni<PageVisitSessionLink> insertPage(UUID deviceId) {
    PageVisitCreateParams payload =
        new PageVisitCreateParams(
            ORGANIZATION_ID,
            deviceId,
            RequestUtils.sneakyUrl("http://localhost:3000"),
            "",
            "testDocType",
            200,
            200,
            200,
            200,
            200);

    return Uni.createFrom()
        .completionStage(
            pageVisitResource
                .create(payload, UserAgentData.MAC__SAFARI)
                .thenApply(
                    response -> {
                      DataResponse<PageVisitSessionLink> dataResponse =
                          response.readEntity(new GenericType<>() {});
                      return dataResponse.getData();
                    }));
  }

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_v1_beacon_beat__should_process__when_single_large_beacon(String contentType)
      throws IOException, URISyntaxException {
    UUID deviceId = UUID.randomUUID();
    PageVisitSessionLink pageVisitSessionLink = insertPage(deviceId).await().indefinitely();
    UUID sessionId = pageVisitSessionLink.getSessionId();
    UUID pageVisitId = pageVisitSessionLink.getPageVisitId();
    String body = Files.readString(Path.of(getClass().getResource("/beacon/initial.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam(UserEventTable.SESSION_ID, sessionId)
        .queryParam(UserEventTable.DEVICE_ID, deviceId)
        .queryParam(UserEventTable.PAGE_VISIT_ID, pageVisitId)
        .queryParam(UserEventTable.ORGANIZATION_ID, ORGANIZATION_ID)
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    assertEquals(382, events.size());
    assertEquals(0, unloadEvents.size());
    events.forEach(
        e -> {
          assertEquals(pageVisitId, e.getPageVisitId());
          assertEquals(sessionId, e.getSessionId());
          assertEquals(deviceId, e.getDeviceId());
          assertEquals(ORGANIZATION_ID, e.getOrganizationId());
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_v1_beacon_beat__should_process__when_multiple_small_beacons(String contentType)
      throws IOException, URISyntaxException {
    UUID deviceId = UUID.randomUUID();
    PageVisitSessionLink pageVisitSessionLink = insertPage(deviceId).await().indefinitely();
    UUID sessionId = pageVisitSessionLink.getSessionId();
    UUID pageVisitId = pageVisitSessionLink.getPageVisitId();
    String body = Files.readString(Path.of(getClass().getResource("/beacon/small.json").toURI()));

    for (int i = 0; i < 100; i++) {
      given()
          .when()
          .contentType(contentType)
          .queryParam(UserEventTable.SESSION_ID, sessionId)
          .queryParam(UserEventTable.DEVICE_ID, deviceId)
          .queryParam(UserEventTable.PAGE_VISIT_ID, pageVisitId)
          .queryParam(UserEventTable.ORGANIZATION_ID, ORGANIZATION_ID)
          .body(body)
          .post(BEACON_RESOURCE_BEAT_PATH)
          .then()
          .statusCode(204);

      assertEquals(i + 1, events.size());
      assertEquals(pageVisitId, events.get(i).getPageVisitId());
      assertEquals(sessionId, events.get(i).getSessionId());
      assertEquals(ORGANIZATION_ID, events.get(i).getOrganizationId());
      assertEquals(deviceId, events.get(i).getDeviceId());
    }

    assertEquals(0, unloadEvents.size());
  }

  @ParameterizedTest
  @ValueSource(strings = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public void post_v1_beacon_beat__should_end_page__when_unload_event(String contentType)
      throws IOException, URISyntaxException {
    UUID deviceId = UUID.randomUUID();
    PageVisitSessionLink pageVisitSessionLink = insertPage(deviceId).await().indefinitely();
    UUID sessionId = pageVisitSessionLink.getSessionId();
    UUID pageVisitId = pageVisitSessionLink.getPageVisitId();

    String body =
        Files.readString(Path.of(getClass().getResource("/beacon/withUnloadEvent.json").toURI()));

    given()
        .when()
        .contentType(contentType)
        .queryParam(UserEventTable.SESSION_ID, sessionId)
        .queryParam(UserEventTable.DEVICE_ID, deviceId)
        .queryParam(UserEventTable.PAGE_VISIT_ID, pageVisitId)
        .queryParam(UserEventTable.ORGANIZATION_ID, ORGANIZATION_ID)
        .body(body)
        .post(BEACON_RESOURCE_BEAT_PATH)
        .then()
        .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    assertEquals(2, events.size());
    assertEquals(1, unloadEvents.size());
    events.forEach(
        e -> {
          assertEquals(pageVisitId, e.getPageVisitId());
          assertEquals(sessionId, e.getSessionId());
          assertEquals(deviceId, e.getDeviceId());
          assertEquals(ORGANIZATION_ID, e.getOrganizationId());
        });

    unloadEvents.forEach(
        e -> {
          assertEquals(pageVisitId, e.getPageVisitId());
          assertEquals(sessionId, e.getSessionId());
          assertEquals(deviceId, e.getDeviceId());
          assertEquals(ORGANIZATION_ID, e.getOrganizationId());
        });
  }
}
