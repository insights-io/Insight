package com.meemaw.events.model.incoming;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.events.model.incoming.BrowserPerformanceEvent.Arguments;

import java.util.Map;

public class BrowserPerformanceEvent extends AbstractBrowserEvent<Arguments> {

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
        arguments.getEntryType(),
        "startTime",
        arguments.getStartTime(),
        "duration",
        arguments.getDuration());
  }

  @Value
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  static class Arguments {
    String name;
    String entryType;
    double startTime;
    double duration;
  }
}
