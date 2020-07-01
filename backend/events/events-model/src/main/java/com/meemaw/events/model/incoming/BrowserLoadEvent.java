package com.meemaw.events.model.incoming;

public class BrowserLoadEvent extends BrowserUnloadEvent {

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.LOAD;
  }
}
