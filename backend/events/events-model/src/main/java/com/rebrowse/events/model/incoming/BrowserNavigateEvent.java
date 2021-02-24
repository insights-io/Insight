package com.rebrowse.events.model.incoming;

import com.rebrowse.events.model.incoming.BrowserNavigateEvent.Arguments;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

public class BrowserNavigateEvent extends AbstractBrowserEvent<Arguments> {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        getEventTypeKey(),
        TIMESTAMP,
        timestamp,
        "location",
        arguments.getLocation(),
        "title",
        arguments.getTitle());
  }

  @Value
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  static class Arguments {
    String location;
    String title;
  }
}
