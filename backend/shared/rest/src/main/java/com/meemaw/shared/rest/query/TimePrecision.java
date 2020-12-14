package com.meemaw.shared.rest.query;

public enum TimePrecision {
  MICROSECONDS("microseconds"),
  MILLISECONDS("milliseconds"),
  SECOND("second"),
  MINUTE("minute"),
  HOUR("hour"),
  DAY("day"),
  WEEK("week"),
  MONTH("month");

  private final String key;

  TimePrecision(String key) {
    this.key = key;
  }

  public static TimePrecision fromString(String key) {
    return TimePrecision.valueOf(key == null ? null : key.toUpperCase());
  }

  public String getKey() {
    return key;
  }
}
