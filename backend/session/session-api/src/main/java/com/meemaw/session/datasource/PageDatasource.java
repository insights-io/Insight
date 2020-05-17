package com.meemaw.session.datasource;

import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import java.util.UUID;

public interface PageDatasource {

  /**
   * @param orgId
   * @param uid
   * @return optionally linked sessionID that has been active in the last 30 minutes
   */
  Uni<Optional<UUID>> findUserSessionLink(String orgId, UUID uid);

  /**
   * @param pageId
   * @param uid
   * @param sessionId
   * @param page
   * @return newly created Page
   */
  Uni<PageIdentity> insertPage(UUID pageId, UUID uid, UUID sessionId, CreatePageDTO page);

  /** @return number of currently active pages */
  Uni<Integer> activePageCount();

  /**
   * @param pageID
   * @param sessionID
   * @param orgID
   * @return
   */
  Uni<Optional<PageDTO>> getPage(UUID pageID, UUID sessionID, String orgID);
}
