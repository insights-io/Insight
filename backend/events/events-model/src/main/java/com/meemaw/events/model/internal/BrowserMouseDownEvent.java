package com.meemaw.events.model.internal;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BrowserMouseDownEvent extends BrowserMouseMoveEvent {

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        BrowserEventTypeConstants.MOUSEDOWN,
        TIMESTAMP,
        timestamp,
        "clientX",
        getClientX(),
        "clientY",
        getClientY());
  }
}
