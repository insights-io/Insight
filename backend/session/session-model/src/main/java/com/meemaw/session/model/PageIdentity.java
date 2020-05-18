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

  UUID uid;
  UUID sessionId;
  UUID pageId;

  /**
   * @param uid user id
   * @param sessionId session id
   * @param pageId page id
   */
  public PageIdentity(UUID uid, UUID sessionId, UUID pageId) {
    this.uid = uid;
    this.sessionId = sessionId;
    this.pageId = pageId;
  }
}
