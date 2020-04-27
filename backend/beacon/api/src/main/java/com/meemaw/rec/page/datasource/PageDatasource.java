package com.meemaw.rec.page.datasource;

import io.smallrye.mutiny.Uni;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PageDatasource {


  /**
   * @param sessionID
   * @param uid
   * @param pageID
   * @return boolean indicating if page exists
   */
  Uni<Boolean> pageExists(UUID sessionID, UUID uid, UUID pageID);

  /**
   * @param pageId
   * @return time of page end if page found
   */
  Uni<Optional<Instant>> pageEnd(UUID pageId);
}
