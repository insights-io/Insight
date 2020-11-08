package com.meemaw.events.model.incoming;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.events.model.incoming.BrowserUnloadEvent.Arguments;

import java.util.Map;

public class BrowserUnloadEvent extends AbstractBrowserEvent<Arguments> {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE, getEventTypeKey(), TIMESTAMP, timestamp, "location", arguments.getLocation());
  }

  @Value
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  public static class Arguments {
    String location;
  }
}
