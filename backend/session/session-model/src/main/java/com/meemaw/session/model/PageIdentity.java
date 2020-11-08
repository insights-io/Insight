package com.meemaw.session.model;

import lombok.*;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PageIdentity {

  UUID deviceId;
  UUID sessionId;
  UUID pageId;
}
