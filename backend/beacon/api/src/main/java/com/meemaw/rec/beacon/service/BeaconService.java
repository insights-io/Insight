package com.meemaw.rec.beacon.service;

import com.meemaw.events.model.external.UserEvent;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import com.meemaw.events.model.internal.BrowserUnloadEvent;
import com.meemaw.events.stream.EventsStream;
import com.meemaw.rec.beacon.model.Beacon;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.resource.v1.SessionResource;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.smallrye.mutiny.Uni;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import lombok.extern.slf4j.Slf4j;
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
  @OnOverflow(value = Strategy.UNBOUNDED_BUFFER)
  Emitter<UserEvent<?>> eventsEmitter;

  @Inject
  @Channel(EventsStream.UNLOAD)
  @OnOverflow(value = Strategy.UNBOUNDED_BUFFER)
  Emitter<UserEvent<?>> unloadEventsEmitter;

  private CompletionStage<Boolean> pageExists(UUID sessionID, UUID pageID, String orgID) {
    return sessionResource
        .get(sessionID, pageID, orgID)
        .exceptionally(
            throwable -> {
              if (throwable.getCause() instanceof WebApplicationException) {
                return ((WebApplicationException) (throwable.getCause())).getResponse();
              }
              throw Boom.serverError().message(throwable.getMessage()).exception();
            })
        .thenApply(
            response -> {
              if (response.getStatus() == 404) {
                return false;
              }
              DataResponse<PageDTO> dataResponse = response.readEntity(new GenericType<>() {});
              PageDTO page = dataResponse.getData();
              return page.getSessionID().equals(sessionID)
                  && page.getId().equals(pageID)
                  && page.getOrgID().equals(orgID);
            });
  }

  /**
   * @param orgID
   * @param sessionID
   * @param uid
   * @param pageID
   * @param beacon
   * @return
   */
  public CompletionStage<?> process(
      String orgID, UUID sessionID, UUID uid, UUID pageID, Beacon beacon) {
    MDC.put("orgID", orgID);
    MDC.put("uid", uid.toString());
    MDC.put("pageID", pageID.toString());
    MDC.put("sessionID", sessionID.toString());

    Function<AbstractBrowserEvent, UserEvent<?>> identify =
        (event) ->
            UserEvent.builder()
                .event(event)
                .orgID(orgID)
                .sessionID(sessionID)
                .pageId(pageID)
                .uid(uid)
                .build();

    return pageExists(sessionID, pageID, orgID)
        .thenApply(
            exists -> {
              if (!exists) {
                log.warn("Unlinked beacon");
                throw Boom.badRequest().message("Unlinked beacon").exception();
              }

              List<AbstractBrowserEvent> events = beacon.getEvents();
              List<Uni<Void>> operations =
                  new LinkedList<>() {
                    {
                      addAll(
                          events.stream()
                              .map(event -> sendEvent(eventsEmitter, identify, event))
                              .collect(Collectors.toList()));
                    }
                  };

              // BrowserUnloadEvent always comes last!
              AbstractBrowserEvent maybeUnloadEvent = events.get(events.size() - 1);
              if (maybeUnloadEvent instanceof BrowserUnloadEvent) {
                operations.add(sendEvent(unloadEventsEmitter, identify, maybeUnloadEvent));
              }

              return Uni.combine().all().unis(operations).combinedWith(nothing -> null);
            });
  }

  private Uni<Void> sendEvent(
      Emitter<UserEvent<?>> channel,
      Function<AbstractBrowserEvent, UserEvent<?>> identify,
      AbstractBrowserEvent event) {
    return Uni.createFrom().completionStage(channel.send(identify.apply(event)));
  }
}
