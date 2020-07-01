package com.meemaw.events.model.incoming;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
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

  protected Map<String, Object> nodeIndex() {
    List<Object> nodeWithAttributes = getNodeWithAttributes();
    Map<String, Object> nodeIndex = new HashMap<>();

    for (int i = 0; i < nodeWithAttributes.size(); i++) {
      Object current = nodeWithAttributes.get(i);
      if (i == 0) {
        nodeIndex.put("type", current);
      } else {
        nodeIndex.put((String) current, nodeWithAttributes.get(++i));
      }
    }

    return nodeIndex;
  }

  @Override
  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(5);
    index.put(EVENT_TYPE, getEventType());
    index.put(TIMESTAMP, timestamp);
    index.put("clientX", getClientX());
    index.put("clientY", getClientY());
    index.put("node", nodeIndex());
    return index;
  }

  @Override
  public String getEventType() {
    return BrowserEventTypeConstants.CLICK;
  }
}
