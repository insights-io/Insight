package com.meemaw.events.model.incoming;

public class BrowserMouseMoveEvent extends BrowserClickEvent {

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.MOUSEMOVE;
  }
}
