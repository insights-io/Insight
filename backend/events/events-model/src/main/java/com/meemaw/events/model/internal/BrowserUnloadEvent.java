package com.meemaw.events.model.internal;

import java.util.Map;

public class BrowserUnloadEvent extends AbstractBrowserEvent {

  public String getLocation() {
    return (String) args.get(0);
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        BrowserEventTypeConstants.UNLOAD,
        TIMESTAMP,
        timestamp,
        "location",
        getLocation());
  }
}
