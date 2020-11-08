package com.meemaw.session.model;

import java.util.UUID;
import lombok.*;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PageIdentity {

  UUID deviceId;
  UUID sessionId;
  UUID pageId;
}
