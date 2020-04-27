package com.meemaw.shared.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BrowserClickEvent extends AbstractBrowserEvent {

  @JsonIgnore
  public int getClientX() {
    return (int) args.get(0);
  }

  @JsonIgnore
  public int getClientY() {
    return (int) args.get(1);
  }

  @JsonIgnore
  public List<Object> getAttributes() {
    int size = args.size();
    if (size <= 3) {
      return Collections.emptyList();
    }
    return args.subList(3, size);
  }

  @JsonIgnore
  public List<Object> getNodeWithAttributes() {
    int size = args.size();
    if (size <= 2) {
      return Collections.emptyList();
    }
    return args.subList(2, size);
  }

  @JsonIgnore
  public Optional<String> getNode() {
    if (args.size() <= 2) {
      return Optional.empty();
    }
    String node = (String) args.get(2);
    return Optional.of(node.substring(1));
  }

  @Override
  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(3);
    index.put("type", BrowserEventTypeConstants.CLICK);
    index.put("timestamp", timestamp);
    index.put("clientX", getClientX());
    index.put("clientY", getClientY());
    return index;
  }
}