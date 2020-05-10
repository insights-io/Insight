package com.meemaw.events.model.internal;

import java.util.Map;

public class BrowserMouseDownEvent extends BrowserMouseMoveEvent {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        "type",
        BrowserEventTypeConstants.MOUSEDOWN,
        "timestamp",
        timestamp,
        "clientX",
        getClientX(),
        "clientY",
        getClientY());
  }
}
