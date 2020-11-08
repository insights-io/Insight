package com.meemaw.events.model.outgoing.dto;

import lombok.*;

import java.util.UUID;

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
