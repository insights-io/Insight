package com.meemaw.events.model.internal;

import java.util.Map;

public class BrowserResizeEvent extends AbstractBrowserEvent {

  public int getInnerWidth() {
    return (int) args.get(0);
  }

  public int getInnerHeight() {
    return (int) args.get(1);
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(
        "type",
        BrowserEventTypeConstants.RESIZE,
        "timestamp",
        timestamp,
        "innerWidth",
        getInnerWidth(),
        "innerHeight",
        getInnerHeight());
  }
}
