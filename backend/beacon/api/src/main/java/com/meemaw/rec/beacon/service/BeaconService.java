package com.meemaw.rec.beacon.service;

import com.meemaw.rec.beacon.datasource.BeaconDatasource;
import com.meemaw.rec.page.datasource.PageDatasource;
import com.meemaw.rec.beacon.model.Beacon;
import com.meemaw.shared.event.EventsChannel;
import com.meemaw.shared.event.model.AbstractBrowserEvent;
import com.meemaw.shared.event.model.AbstractBrowserEventBatch;
import com.meemaw.shared.event.model.BrowserUnloadEvent;
import com.meemaw.shared.rest.response.Boom;
import io.smallrye.mutiny.Uni;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
  @Channel(EventsChannel.ALL)
  @OnOverflow(value = Strategy.UNBOUNDED_BUFFER)
  Emitter<AbstractBrowserEventBatch> eventsEmitter;

  @Inject
  @Channel(EventsChannel.UNLOAD)
  @OnOverflow(value = Strategy.UNBOUNDED_BUFFER)
  Emitter<AbstractBrowserEvent> unloadEventsEmitter;

  public Uni<Void> pageEnd(UUID pageId) {
    return pageDatasource.pageEnd(pageId)
        .onItem()
        .apply(maybePageEnd -> {
          if (maybePageEnd.isEmpty()) {
            log.warn("Page end missing pageId={}", pageId);
          } else {
            log.info("Page end at {} for pageId={}", maybePageEnd.get(), pageId);
          }
          return null;
        });
  }

  public Uni<?> process(UUID sessionID, UUID uid, UUID pageID, Beacon beacon) {
    return pageDatasource.pageExists(sessionID, uid, pageID).onItem().produceUni(exists -> {
      if (!exists) {
        log.warn("Unlinked beacon sessionID={} uid={} pageId={}", sessionID, uid, pageID);
        throw Boom.badRequest().message("Unlinked beacon").exception();
      }

      List<AbstractBrowserEvent> events = beacon.getEvents();
      List<Uni<Void>> operations = new ArrayList<>(3) {
        {
          add(sendEvents(events, pageID));
          add(beaconDatasource.store(beacon));
        }
      };

      // BrowserUnloadEvent always comes last!
      AbstractBrowserEvent maybeUnloadEvent = events.get(events.size() - 1);
      if (maybeUnloadEvent instanceof BrowserUnloadEvent) {
        operations.add(pageEnd(pageID));
        unloadEventsEmitter.send(maybeUnloadEvent);
      }

      return Uni.combine().all().unis(operations).combinedWith(nothing -> null);
    });
  }

  private Uni<Void> sendEvents(List<AbstractBrowserEvent> events, UUID pageID) {
    AbstractBrowserEventBatch batch = AbstractBrowserEventBatch.builder().events(events)
        .pageId(pageID).build();

    return Uni.createFrom().completionStage(eventsEmitter.send(batch));
  }
}
