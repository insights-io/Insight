package com.meemaw.events.model.external;

import com.meemaw.events.model.internal.AbstractBrowserEvent;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BrowserEvent<T extends AbstractBrowserEvent> {

  T event;
  UUID pageId;
  UUID sessionID;
  UUID uid;
  String orgID;

}
