package com.meemaw.events.model.internal;

import java.util.Map;
import lombok.Builder;

@Builder
public class BrowserLoadEvent extends BrowserUnloadEvent {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        "type", BrowserEventTypeConstants.LOAD, "timestamp", timestamp, "location", getLocation());
  }
}
