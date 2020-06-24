package com.meemaw.events.model.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BrowserClickEvent extends AbstractBrowserEvent {

  private static final int NODE_INDEX_START = 2;

  public int getClientX() {
    return (int) args.get(0);
  }

  public int getClientY() {
    return (int) args.get(1);
  }

  @JsonIgnore
  public List<Object> getNodeWithAttributes() {
    int size = args.size();
    if (size <= NODE_INDEX_START) {
      return Collections.emptyList();
    }
    return args.subList(NODE_INDEX_START, size);
  }

  @JsonIgnore
  public Optional<String> getNode() {
    if (args.size() <= NODE_INDEX_START) {
      return Optional.empty();
    }
    String node = (String) args.get(NODE_INDEX_START);
    return Optional.of(node.substring(1));
  }

  @Override
  public Map<String, Object> index() {
    return Map.of(
        EVENT_TYPE,
        BrowserEventTypeConstants.CLICK,
        TIMESTAMP,
        timestamp,
        "clientX",
        getClientX(),
        "clientY",
        getClientY());
  }
}
