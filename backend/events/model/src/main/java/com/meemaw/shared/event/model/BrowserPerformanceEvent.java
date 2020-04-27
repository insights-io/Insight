package com.meemaw.shared.event.model;

import java.util.HashMap;
import java.util.Map;

public class BrowserPerformanceEvent extends AbstractBrowserEvent {

  public String getName() {
    return (String) args.get(0);
  }

  public String getEntryType() {
    return (String) args.get(1);
  }

  public double getStartTime() {
    Object startTime = args.get(2);
    if (startTime instanceof Integer) {
      return (int) startTime;
    } else {
      return (Double) startTime;
    }
  }

  public double getDuration() {
    Object duration = args.get(3);
    if (duration instanceof Integer) {
      return (int) duration;
    } else {
      return (Double) duration;
    }
  }

  @Override
  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(6);
    index.put("type", BrowserEventTypeConstants.PERFORMANCE);
    index.put("timestamp", timestamp);
    index.put("name", getName());
    index.put("entryType", getEntryType());
    index.put("startTime", getStartTime());
    index.put("duration", getDuration());
    return index;
  }
}
