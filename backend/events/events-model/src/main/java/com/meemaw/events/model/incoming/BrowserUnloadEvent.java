package com.meemaw.events.model.incoming;

import java.util.Map;

public class BrowserUnloadEvent extends AbstractBrowserEvent {

  public String getLocation() {
    return (String) args.get(0);
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(EVENT_TYPE, getEventType(), TIMESTAMP, timestamp, "location", getLocation());
  }

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.UNLOAD;
  }
}
