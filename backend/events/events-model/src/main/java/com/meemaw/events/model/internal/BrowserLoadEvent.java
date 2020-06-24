package com.meemaw.events.model.internal;

import java.util.Map;

public class BrowserLoadEvent extends BrowserUnloadEvent {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        BrowserEventTypeConstants.LOAD,
        TIMESTAMP,
        timestamp,
        "location",
        getLocation());
  }
}
