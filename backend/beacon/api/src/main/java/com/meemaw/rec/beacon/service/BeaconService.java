package com.meemaw.rec.beacon.service;

import com.meemaw.events.model.external.UserEvent;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import com.meemaw.events.model.internal.BrowserUnloadEvent;
import com.meemaw.events.stream.EventsStream;
import com.meemaw.rec.beacon.datasource.BeaconDatasource;
import com.meemaw.rec.beacon.model.Beacon;
import com.meemaw.rec.page.datasource.PageDatasource;
import com.meemaw.shared.rest.response.Boom;
import io.smallrye.mutiny.Uni;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.eclipse.microprofile.reactive.messaging.OnOverflow.Strategy;

@ApplicationScoped
@Slf4j
public class BeaconService {

  @Inject
  BeaconDatasource beaconDatasource;

  @Inject
  PageDatasource pageDatasource;

  @Inject
  @Channel(EventsStream.ALL)
  @OnOverflow(value = Strategy.UNBOUNDED_BUFFER)
  Emitter<UserEvent<?>> eventsEmitter;

  @Inject
  @Channel(EventsStream.UNLOAD)
  @OnOverflow(value = Strategy.UNBOUNDED_BUFFER)
  Emitter<UserEvent<?>> unloadEventsEmitter;

  public Uni<Void> pageEnd(String orgID, UUID pageId) {
    return pageDatasource
        .pageEnd(orgID, pageId)
        .onItem()
        .apply(
            maybePageEnd -> {
              if (maybePageEnd.isEmpty()) {
                log.warn("Page end missing orgID={} pageId={}", orgID, pageId);
              } else {
                log.info(
                    "Page end at {} for orgID={} pageId={}", maybePageEnd.get(), orgID, pageId);
              }
              return null;
            });
  }

  public Uni<?> process(String orgID, UUID sessionID, UUID uid, UUID pageID, Beacon beacon) {
    Function<AbstractBrowserEvent, UserEvent<?>> identify =
        (event) ->
            UserEvent.builder()
                .event(event)
                .orgID(orgID)
                .sessionID(sessionID)
                .pageId(pageID)
                .uid(uid)
                .build();

    return pageDatasource
        .pageExists(orgID, sessionID, uid, pageID)
        .onItem()
        .produceUni(
            exists -> {
              if (!exists) {
                log.warn(
                    "Unlinked beacon orgID={} sessionID={} uid={} pageId={}",
                    orgID,
                    sessionID,
                    uid,
                    pageID);
                throw Boom.badRequest().message("Unlinked beacon").exception();
              }

              List<AbstractBrowserEvent> events = beacon.getEvents();
              int operationCount = beacon.getEvents().size() + 3;
              List<Uni<Void>> operations =
                  new ArrayList<>(operationCount) {
                    {
                      addAll(
                          events.stream()
                              .map(event -> sendEvent(eventsEmitter, identify, event))
                              .collect(Collectors.toList()));
                      add(beaconDatasource.store(beacon));
                    }
                  };

              // BrowserUnloadEvent always comes last!
              AbstractBrowserEvent maybeUnloadEvent = events.get(events.size() - 1);
              if (maybeUnloadEvent instanceof BrowserUnloadEvent) {
                operations.add(pageEnd(orgID, pageID));
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
