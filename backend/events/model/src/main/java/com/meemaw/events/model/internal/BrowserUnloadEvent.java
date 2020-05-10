package com.meemaw.events.model.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

public class BrowserUnloadEvent extends AbstractBrowserEvent {

  @JsonIgnore
  public String getLocation() {
    return (String) args.get(0);
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(
        "type",
        BrowserEventTypeConstants.UNLOAD,
        "timestamp",
        timestamp,
        "location",
        getLocation());
  }
}
