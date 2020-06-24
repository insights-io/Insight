package com.meemaw.events.model.internal;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
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
