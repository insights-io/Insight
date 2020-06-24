package com.meemaw.events.model.internal;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
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
        EVENT_TYPE,
        BrowserEventTypeConstants.RESIZE,
        TIMESTAMP,
        timestamp,
        "innerWidth",
        getInnerWidth(),
        "innerHeight",
        getInnerHeight());
  }
}
