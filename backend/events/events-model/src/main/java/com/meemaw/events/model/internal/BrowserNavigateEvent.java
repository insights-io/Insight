package com.meemaw.events.model.internal;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BrowserNavigateEvent extends AbstractBrowserEvent {

  public String getLocation() {
    return (String) args.get(0);
  }

  public String getTitle() {
    return (String) args.get(1);
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        BrowserEventTypeConstants.NAVIGATE,
        TIMESTAMP,
        timestamp,
        "location",
        getLocation(),
        "title",
        getTitle());
  }
}
