package com.meemaw.events.model.internal;

public class BrowserMouseUpEvent extends BrowserMouseMoveEvent {

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.MOUSEUP;
  }
}
