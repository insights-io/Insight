package com.meemaw.events.model.external.dto;

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
public class UserEventDTO<T extends AbstractBrowserEventDTO> {

  T event;
  UUID pageId;
  UUID sessionId;
  UUID deviceId;
  String organizationId;
}
