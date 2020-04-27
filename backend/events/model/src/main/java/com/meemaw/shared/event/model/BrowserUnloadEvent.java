package com.meemaw.shared.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

public class BrowserUnloadEvent extends AbstractBrowserEvent {

  @JsonIgnore
  public String getLocation() {
    return (String) args.get(0);
  }

  @Override
  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(3);
    index.put("type", BrowserEventTypeConstants.UNLOAD);
    index.put("timestamp", timestamp);
    index.put("location", getLocation());
    return index;
  }
}
