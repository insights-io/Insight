package com.meemaw.shared.event.model;

import java.util.HashMap;
import java.util.Map;

public class BrowserMouseMoveEvent extends BrowserClickEvent {

  @Override
  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(4);
    index.put("type", BrowserEventTypeConstants.MOUSEMOVE);
    index.put("timestamp", timestamp);
    index.put("clientX", getClientX());
    index.put("clientY", getClientY());
    return index;
  }
}
