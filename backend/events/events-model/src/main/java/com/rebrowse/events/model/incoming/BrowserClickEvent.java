package com.rebrowse.events.model.incoming;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BrowserClickEvent extends AbstractBrowserEvent<List<Object>> {

  private static final int NODE_INDEX_START = 2;

  @JsonIgnore
  public int getClientX() {
    return (int) arguments.get(0);
  }

  @JsonIgnore
  public int getClientY() {
    return (int) arguments.get(1);
  }

  @JsonIgnore
  public List<Object> getNodeWithAttributes() {
    int size = arguments.size();
    if (size <= NODE_INDEX_START) {
      return Collections.emptyList();
    }
    return arguments.subList(NODE_INDEX_START, size);
  }

  @JsonIgnore
  public Optional<String> getNode() {
    if (arguments.size() <= NODE_INDEX_START) {
      return Optional.empty();
    }
    String node = (String) arguments.get(NODE_INDEX_START);
    return Optional.of(node.substring(1));
  }

  protected Map<String, Object> nodeIndex() {
    List<Object> nodeWithAttributes = getNodeWithAttributes();
    Map<String, Object> nodeIndex = new HashMap<>();

    for (int i = 0; i < nodeWithAttributes.size(); i++) {
      String current = (String) nodeWithAttributes.get(i);
      if (i == 0) {
        nodeIndex.put("type", current);
      } else {
        nodeIndex.put(current, nodeWithAttributes.get(++i));
      }
    }

    return nodeIndex;
  }

  @Override
  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(5);
    index.put(EVENT_TYPE, getEventTypeKey());
    index.put(TIMESTAMP, timestamp);
    index.put("clientX", getClientX());
    index.put("clientY", getClientY());
    index.put("node", nodeIndex());
    return index;
  }
}
