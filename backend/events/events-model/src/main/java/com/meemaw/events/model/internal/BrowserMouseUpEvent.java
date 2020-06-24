package com.meemaw.events.model.internal;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
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
