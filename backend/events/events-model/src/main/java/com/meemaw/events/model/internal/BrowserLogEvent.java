package com.meemaw.events.model.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;

public class BrowserLogEvent extends AbstractBrowserEvent {

  @JsonIgnore
  public String getLogLevel() {
    return (String) args.get(0);
  }

  @JsonIgnore
  public List<Object> getArguments() {
    return args.subList(1, args.size());
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        getEventType(),
        TIMESTAMP,
        timestamp,
        "level",
        getLogLevel(),
        "arguments",
        getArguments());
  }

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.LOG;
  }
}
