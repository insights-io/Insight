package com.meemaw.shared.event.model;

import java.util.HashMap;
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
    Map<String, Object> index = new HashMap<>(4);
    index.put("type", BrowserEventTypeConstants.RESIZE);
    index.put("timestamp", timestamp);
    index.put("innerWidth", getInnerWidth());
    index.put("innerHeight", getInnerHeight());
    return index;
  }
}
