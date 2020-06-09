package com.meemaw.session.model;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PageIdentity {

  UUID deviceId;
  UUID sessionId;
  UUID pageId;

  /**
   * @param deviceId UUID device id
   * @param sessionId UUID session id
   * @param pageId UUID page id
   */
  public PageIdentity(UUID deviceId, UUID sessionId, UUID pageId) {
    this.deviceId = deviceId;
    this.sessionId = sessionId;
    this.pageId = pageId;
  }
}
