package com.rebrowse.events.model.incoming;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebrowse.events.model.shared.LogLevel;
import java.util.List;
import java.util.Map;

public class BrowserLogEvent extends AbstractBrowserEvent<List<?>> {

  @JsonIgnore
  public LogLevel getLevel() {
    return LogLevel.fromString((String) arguments.get(0));
  }

  @JsonIgnore
  @Override
  public List<?> getArguments() {
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
