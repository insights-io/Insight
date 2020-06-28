package com.meemaw.events.model.internal;

public class BrowserLoadEvent extends BrowserUnloadEvent {

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.LOAD;
  }
}
