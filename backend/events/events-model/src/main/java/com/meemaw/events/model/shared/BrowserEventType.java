package com.meemaw.events.model.shared;

import lombok.Getter;
import lombok.ToString;

@ToString
public enum BrowserEventType {
  NAVIGATE(BrowserEventType.NAVIGATE_KEY),
  UNLOAD(BrowserEventType.UNLOAD_KEY),
  RESIZE(BrowserEventType.RESIZE_KEY),
  PERFORMANCE(BrowserEventType.PERFORMANCE_KEY),
  CLICK(BrowserEventType.CLICK_KEY),
  MOUSEMOVE(BrowserEventType.MOUSEMOVE_KEY),
  MOUSEDOWN(BrowserEventType.MOUSEDOWN_KEY),
  MOUSEUP(BrowserEventType.MOUSEUP_KEY),
  LOAD(BrowserEventType.LOAD_KEY),
  LOG(BrowserEventType.LOG_KEY),
  ERROR(BrowserEventType.ERROR_KEY);

  @Getter private final byte key;

  BrowserEventType(String key) {
    this.key = Byte.parseByte(key);
  }

  public static final String NAVIGATE_KEY = "0";
  public static final String UNLOAD_KEY = "1";
  public static final String RESIZE_KEY = "2";
  public static final String PERFORMANCE_KEY = "3";
  public static final String CLICK_KEY = "4";
  public static final String MOUSEMOVE_KEY = "5";
  public static final String MOUSEDOWN_KEY = "6";
  public static final String MOUSEUP_KEY = "7";
  public static final String LOAD_KEY = "8";
  public static final String LOG_KEY = "9";
  public static final String ERROR_KEY = "10";
}
