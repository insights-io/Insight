package com.rebrowse.events.model.incoming;

import com.rebrowse.events.model.incoming.BrowserXhrEvent.Arguments;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

public class BrowserXhrEvent extends AbstractBrowserEvent<Arguments> {

  @Override
  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(8);
    index.put(EVENT_TYPE, getEventTypeKey());
    index.put(TIMESTAMP, timestamp);
    index.put("method", arguments.getMethod());
    index.put("url", arguments.getUrl());
    index.put("status", arguments.getStatus());
    index.put("type", arguments.getType());
    index.put("initiatorType", arguments.getInitiatorType());
    index.put("nextHopProtocol", arguments.getNextHopProtocol());
    return index;
  }

  @Value
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  public static class Arguments {
    String method;
    String url;
    int status;
    String type;
    String initiatorType;
    String nextHopProtocol; // extracted from PerformanceResourceTiming
  }
}
