package com.meemaw.events.model.incoming;

public class BrowserMouseUpEvent extends BrowserMouseMoveEvent {

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.MOUSEUP;
  }
}
