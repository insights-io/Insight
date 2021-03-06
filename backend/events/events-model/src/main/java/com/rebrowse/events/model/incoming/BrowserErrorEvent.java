package com.rebrowse.events.model.incoming;

import com.rebrowse.events.model.incoming.BrowserErrorEvent.Arguments;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

public class BrowserErrorEvent extends AbstractBrowserEvent<Arguments> {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        getEventTypeKey(),
        TIMESTAMP,
        timestamp,
        "message",
        arguments.getMessage(),
        "name",
        arguments.getName(),
        "stack",
        arguments.getStack());
  }

  @Value
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  static class Arguments {
    String message;
    String name;
    String stack;
  }
}
