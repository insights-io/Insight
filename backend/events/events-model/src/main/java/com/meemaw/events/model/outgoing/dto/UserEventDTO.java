package com.meemaw.events.model.outgoing.dto;

import java.util.UUID;
import lombok.*;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserEventDTO<T extends AbstractBrowserEventDTO> {

  T event;
  UUID pageId;
  UUID sessionId;
  UUID deviceId;
  String organizationId;
}
