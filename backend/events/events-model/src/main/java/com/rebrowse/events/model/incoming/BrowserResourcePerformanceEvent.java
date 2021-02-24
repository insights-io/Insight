package com.rebrowse.events.model.incoming;

import com.rebrowse.events.model.incoming.BrowserResourcePerformanceEvent.Arguments;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

public class BrowserResourcePerformanceEvent extends AbstractBrowserEvent<Arguments> {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        getEventTypeKey(),
        TIMESTAMP,
        timestamp,
        "name",
        arguments.getName(),
        "entryType",
        "resource",
        "startTime",
        arguments.getStartTime(),
        "duration",
        arguments.getDuration(),
        "initiatorType",
        arguments.getInitiatorType(),
        "nextHopProtocol",
        arguments.getNextHopProtocol());
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  static class Arguments extends AbstractBrowserPerformanceEventArguments {

    String initiatorType;
    String nextHopProtocol;
  }
}
