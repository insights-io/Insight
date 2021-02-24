package com.rebrowse.events.model.incoming;

import com.rebrowse.events.model.incoming.BrowserResizeEvent.Arguments;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

public class BrowserResizeEvent extends AbstractBrowserEvent<Arguments> {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        getEventTypeKey(),
        TIMESTAMP,
        timestamp,
        "innerWidth",
        arguments.getInnerWidth(),
        "innerHeight",
        arguments.getInnerHeight());
  }

  @Value
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  static class Arguments {
    int innerWidth;
    int innerHeight;
  }
}
