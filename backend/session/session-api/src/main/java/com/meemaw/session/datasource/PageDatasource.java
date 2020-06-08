package com.meemaw.session.datasource;

import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import java.util.UUID;

public interface PageDatasource {

  /**
   * @param organizationId String organization id
   * @param deviceId String device id
   * @return optionally linked sessionID that has been active in the last 30 minutes
   */
  Uni<Optional<UUID>> findUserSessionLink(String organizationId, UUID deviceId);

  /**
   * @param pageId String page id
   * @param deviceId String device id
   * @param sessionId String session id
   * @param page CreatePageDTO page
   * @return newly created Page
   */
  Uni<PageIdentity> insertPage(UUID pageId, UUID deviceId, UUID sessionId, CreatePageDTO page);

  /** @return number of currently active pages */
  Uni<Integer> activePageCount();

  /**
   * @param pageId String page id
   * @param sessionId String session id
   * @param organizationId String organization id
   * @return maybe page
   */
  Uni<Optional<PageDTO>> getPage(UUID pageId, UUID sessionId, String organizationId);
}
