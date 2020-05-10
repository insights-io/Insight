package com.meemaw.events.model.internal;

import java.util.Map;

public class BrowserNavigateEvent extends AbstractBrowserEvent {

  public String getLocation() {
    return (String) args.get(0);
  }

  public String getTitle() {
    return (String) args.get(1);
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(
        "type",
        BrowserEventTypeConstants.NAVIGATE,
        "timestamp",
        timestamp,
        "location",
        getLocation(),
        "title",
        getTitle());
  }
}
