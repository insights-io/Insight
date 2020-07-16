package com.meemaw.events.model.incoming;

import com.meemaw.events.model.incoming.BrowserFetchEvent.Arguments;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

public class BrowserFetchEvent extends AbstractBrowserEvent<Arguments> {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        getEventTypeKey(),
        TIMESTAMP,
        timestamp,
        "method",
        arguments.getMethod(),
        "url",
        arguments.getUrl(),
        "status",
        arguments.getStatus(),
        "type",
        arguments.getType());
  }

  @Value
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  public static class Arguments {
    String method;
    String url;
    int status;
    String type;
  }
}
