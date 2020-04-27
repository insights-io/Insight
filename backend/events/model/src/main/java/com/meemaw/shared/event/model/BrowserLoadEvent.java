package com.meemaw.shared.event.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;

@Builder
public class BrowserLoadEvent extends BrowserUnloadEvent {

  @Override
  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(3);
    index.put("type", BrowserEventTypeConstants.LOAD);
    index.put("timestamp", timestamp);
    index.put("location", getLocation());
    return index;
  }
}
