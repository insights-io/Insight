package com.rebrowse.events.model.incoming;

import com.rebrowse.events.model.incoming.BrowserUnloadEvent.Arguments;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

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
