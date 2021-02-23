package com.rebrowse.events.model.incoming;

import com.rebrowse.events.index.UserEventIndex;
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
public class UserEvent<T extends AbstractBrowserEvent<?>> {

  T event;
  UUID pageVisitId;
  UUID sessionId;
  UUID deviceId;
  String organizationId;

  public Map<String, Object> index() {
    Map<String, Object> index = new HashMap<>(5);
    index.put(UserEventIndex.EVENT.getName(), event.index());
    index.put(UserEventIndex.PAGE_VISIT_ID.getName(), pageVisitId.toString());
    index.put(UserEventIndex.SESSION_ID.getName(), sessionId.toString());
    index.put(UserEventIndex.DEVICE_ID.getName(), deviceId.toString());
    index.put(UserEventIndex.ORGANIZATION_ID.getName(), organizationId);
    return index;
  }
}
