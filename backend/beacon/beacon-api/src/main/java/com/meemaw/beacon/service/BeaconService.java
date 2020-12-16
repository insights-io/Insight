package com.meemaw.beacon.service;

import com.meemaw.beacon.model.Beacon;
import com.meemaw.events.model.incoming.AbstractBrowserEvent;
import com.meemaw.events.model.incoming.BrowserUnloadEvent;
import com.meemaw.events.model.incoming.UserEvent;
import com.meemaw.events.stream.EventsStream;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import com.rebrowse.exception.ApiException;
import com.rebrowse.model.session.SessionPage;
import com.rebrowse.net.RequestOptions;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.eclipse.microprofile.reactive.messaging.OnOverflow.Strategy;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class BeaconService {

  @ConfigProperty(name = "session-api/mp-rest/url")
  String sessionApiBaseUrl;

  @Inject
  @Channel(EventsStream.ALL)
  @OnOverflow(Strategy.UNBOUNDED_BUFFER)
  Emitter<UserEvent<?>> eventsEmitter;

  @Inject
  @Channel(EventsStream.UNLOAD)
  @OnOverflow(Strategy.UNBOUNDED_BUFFER)
  Emitter<UserEvent<?>> unloadEventsEmitter;

  @Traced
  @Timed(
      name = "pageVisitExists",
      description = "A measure of how long it takes to check if page visit exists")
  CompletionStage<Boolean> pageVisitExists(UUID id, String organizationId) {
    return SessionPage.retrieve(
            id, organizationId, new RequestOptions.Builder().apiBaseUrl(sessionApiBaseUrl).build())
        .thenApply(sessionPage -> sessionPage.getId().equals(id))
        .exceptionally(
            throwable -> {
              CompletionException completionException = (CompletionException) throwable;
              Throwable cause = throwable.getCause();
              if (cause instanceof ApiException && ((ApiException) cause).getStatusCode() == 404) {
                return false;
              }
              throw completionException;
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
   * @param pageVisitId String page visit id
   * @param beacon Beacon
   * @return CompletionStage if successful processing
   */
  @Traced
  @Timed(name = "processBeacon", description = "A measure of how long it takes to process beacon")
  public CompletionStage<?> process(
      String organizationId, UUID sessionId, UUID deviceId, UUID pageVisitId, Beacon beacon) {
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    MDC.put(LoggingConstants.DEVICE_ID, deviceId.toString());
    MDC.put(LoggingConstants.PAGE_VISIT_ID, pageVisitId.toString());
    MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());

    Function<AbstractBrowserEvent<?>, UserEvent<?>> identify =
        (event) ->
            UserEvent.builder()
                .event(event)
                .organizationId(organizationId)
                .sessionId(sessionId)
                .pageVisitId(pageVisitId)
                .deviceId(deviceId)
                .build();

    List<AbstractBrowserEvent<?>> events = beacon.getEvents();
    log.info(
        "[BEACON]: Processing beacon sequence: {} size: {}", beacon.getSequence(), events.size());

    return pageVisitExists(pageVisitId, organizationId)
        .thenApply(
            exists -> {
              if (!exists) {
                log.debug("[BEACON]: Unlinked beacon, ignoring sessionId={}", sessionId);
                throw Boom.badRequest().message("Unlinked beacon").exception();
              }

              log.debug("[BEACON]: Sending beacon events to Kafka sessionId={}", sessionId);
              Stream<Uni<Void>> operations =
                  events.stream().map(event -> sendEvent(identify, event));

              // BrowserUnloadEvent always comes last!
              AbstractBrowserEvent<?> maybeUnloadEvent = events.get(events.size() - 1);
              if (maybeUnloadEvent instanceof BrowserUnloadEvent) {
                log.debug("[BEACON]: Sending BrowserUnloadEvent to Kafka sessionId={}", sessionId);
                operations =
                    Stream.concat(
                        operations, Stream.of(sendUnloadEvent(identify, maybeUnloadEvent)));
              }

              return Uni.combine()
                  .all()
                  .unis(operations.collect(Collectors.toList()))
                  .combinedWith(nothing -> null);
            });
  }

  private Uni<Void> sendEvent(
      Function<AbstractBrowserEvent<?>, UserEvent<?>> identify, AbstractBrowserEvent<?> event) {
    return sendEventToChannel(eventsEmitter, identify, event);
  }

  private Uni<Void> sendUnloadEvent(
      Function<AbstractBrowserEvent<?>, UserEvent<?>> identify, AbstractBrowserEvent<?> event) {
    return sendEventToChannel(unloadEventsEmitter, identify, event);
  }

  private Uni<Void> sendEventToChannel(
      Emitter<UserEvent<?>> channel,
      Function<AbstractBrowserEvent<?>, UserEvent<?>> identify,
      AbstractBrowserEvent<?> event) {
    return Uni.createFrom().completionStage(channel.send(identify.apply(event)));
  }
}
