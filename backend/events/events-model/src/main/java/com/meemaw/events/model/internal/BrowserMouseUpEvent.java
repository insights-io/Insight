package com.meemaw.events.model.internal;

import java.util.Map;

public class BrowserMouseUpEvent extends BrowserMouseMoveEvent {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        "type",
        BrowserEventTypeConstants.MOUSEUP,
        "timestamp",
        timestamp,
        "clientX",
        getClientX(),
        "clientY",
        getClientY());
  }
}
