package com.meemaw.events.model.incoming;

public class BrowserMouseDownEvent extends BrowserMouseMoveEvent {

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.MOUSEDOWN;
  }
}
