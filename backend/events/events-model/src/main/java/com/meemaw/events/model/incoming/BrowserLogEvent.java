package com.meemaw.events.model.incoming;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meemaw.events.model.shared.LogLevel;
import java.util.List;
import java.util.Map;

public class BrowserLogEvent extends AbstractBrowserEvent<List<String>> {

  @JsonIgnore
  public LogLevel getLevel() {
    return LogLevel.fromString(arguments.get(0));
  }

  @JsonIgnore
  public List<String> getArguments() {
    return arguments.subList(1, arguments.size());
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        getEventTypeKey(),
        TIMESTAMP,
        timestamp,
        "level",
        getLevel(),
        "arguments",
        getArguments());
  }
}
