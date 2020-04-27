package com.meemaw.shared.event.model;

import java.util.HashMap;
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
    Map<String, Object> index = new HashMap<>(4);
    index.put("type", BrowserEventTypeConstants.NAVIGATE);
    index.put("timestamp", timestamp);
    index.put("location", getLocation());
    index.put("title", getTitle());
    return index;
  }
}
