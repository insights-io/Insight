package com.meemaw.events.model.internal;

public class BrowserMouseDownEvent extends BrowserMouseMoveEvent {

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.MOUSEDOWN;
  }
}
