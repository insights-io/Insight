package com.meemaw.events.model.external;

import com.meemaw.events.model.internal.AbstractBrowserEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserEvent<T extends AbstractBrowserEvent> {

  T event;
  UUID pageId;
  UUID sessionID;
  UUID uid;
  String orgID;

  public Map<String, Object> index() {
    Map<String, Object> eventIndex = event.index();
    Map<String, Object> index = new HashMap<>(4 + eventIndex.size());
    index.put("pageId", pageId.toString());
    index.put("sessionID", sessionID.toString());
    index.put("uid", uid.toString());
    index.put("orgID", orgID);
    index.putAll(eventIndex);
    return index;
  }
}
