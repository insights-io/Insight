package com.rebrowse.events.model.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LogLevel {
  INFO,
  ERROR,
  WARN,
  LOG,
  DEBUG;

  @JsonValue
  public String getKey() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static LogLevel fromString(String key) {
    return LogLevel.valueOf(key.toUpperCase());
  }
}
