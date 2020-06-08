package com.meemaw.rec.beacon.service;

import com.meemaw.events.model.external.UserEvent;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import com.meemaw.events.model.internal.BrowserUnloadEvent;
import com.meemaw.events.stream.EventsStream;
import com.meemaw.rec.beacon.model.Beacon;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.resource.v1.SessionResource;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.eclipse.microprofile.reactive.messaging.OnOverflow.Strategy;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class BeaconService {

  @Inject @RestClient SessionResource sessionResource;

  @Inject
  @Channel(EventsStream.ALL)
  @OnOverflow(Strategy.UNBOUNDED_BUFFER)
  Emitter<UserEvent<?>> eventsEmitter;

  @Inject
  @Channel(EventsStream.UNLOAD)
  @OnOverflow(Strategy.UNBOUNDED_BUFFER)
  Emitter<UserEvent<?>> unloadEventsEmitter;

  private CompletionStage<Boolean> pageExists(UUID sessionId, UUID pageId, String organizationId) {
    return sessionResource
        .getPage(sessionId, pageId, organizationId)
        .exceptionally(
            throwable -> {
              if (throwable.getCause() instanceof WebApplicationException) {
                return ((WebApplicationException) (throwable.getCause())).getResponse();
              }
              log.error("Unexpected exception", throwable);
              throw Boom.serverError().message(throwable.getMessage()).exception();
            })
        .thenApply(
            response -> {
              if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
                return false;
              }
              DataResponse<PageDTO> dataResponse = response.readEntity(new GenericType<>() {});
              PageDTO page = dataResponse.getData();
              return page.getSessionId().equals(sessionId)
                  && page.getId().equals(pageId)
                  && page.getOrganizationId().equals(organizationId);
            });
  }

  /**
   * Process a beacon of events. First figure out if page is associated with any of the existing
   * pages, to prevent malicious data injection.
   *
   * <p>After, send all events to Kafka stream.
   *
   * @param organizationId String organization id
   * @param sessionId String session id
   * @param deviceId String user (device) id
   * @param pageId String page id
   * @param beacon Beacon
   * @return CompletionStage if successful processing
   */
  @Timed(name = "processBeacon", description = "A measure of how long it takes to process beacon")
  public CompletionStage<?> process(
      String organizationId, UUID sessionId, UUID deviceId, UUID pageId, Beacon beacon) {
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    MDC.put(LoggingConstants.DEVICE_ID, deviceId.toString());
    MDC.put(LoggingConstants.PAGE_ID, pageId.toString());
    MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());

    Function<AbstractBrowserEvent, UserEvent<?>> identify =
        (event) ->
            UserEvent.builder()
                .event(event)
                .organizationId(organizationId)
                .sessionId(sessionId)
                .pageId(pageId)
                .deviceId(deviceId)
                .build();

    log.info("Processing beacon");
    return pageExists(sessionId, pageId, organizationId)
        .thenApply(
            exists -> {
              if (!exists) {
                log.warn("Unlinked beacon, ignoring ...");
                throw Boom.badRequest().message("Unlinked beacon").exception();
              }
              log.info("Sending {} beacon events to Kafka", beacon.getEvents().size());

              List<AbstractBrowserEvent> events = beacon.getEvents();
              Stream<Uni<Void>> operations =
                  events.stream().map(event -> sendEvent(eventsEmitter, identify, event));

              // BrowserUnloadEvent always comes last!
              AbstractBrowserEvent maybeUnloadEvent = events.get(events.size() - 1);
              if (maybeUnloadEvent instanceof BrowserUnloadEvent) {
                log.info("Sending BrowserUnloadEvent to Kafka");
                operations =
                    Stream.concat(
                        operations,
                        Stream.of(sendEvent(unloadEventsEmitter, identify, maybeUnloadEvent)));
              }

              return Uni.combine()
                  .all()
                  .unis(operations.collect(Collectors.toList()))
                  .combinedWith(nothing -> null);
            });
  }

  private Uni<Void> sendEvent(
      Emitter<UserEvent<?>> channel,
      Function<AbstractBrowserEvent, UserEvent<?>> identify,
      AbstractBrowserEvent event) {
    return Uni.createFrom().completionStage(channel.send(identify.apply(event)));
  }
}
