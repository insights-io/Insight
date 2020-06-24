package com.meemaw.events.model.internal;

import java.util.Map;

public class BrowserMouseMoveEvent extends BrowserClickEvent {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        BrowserEventTypeConstants.MOUSEMOVE,
        TIMESTAMP,
        timestamp,
        "clientX",
        getClientX(),
        "clientY",
        getClientY());
  }
}
