package com.meemaw.events.model.internal;

public class BrowserMouseMoveEvent extends BrowserClickEvent {

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.MOUSEMOVE;
  }
}
