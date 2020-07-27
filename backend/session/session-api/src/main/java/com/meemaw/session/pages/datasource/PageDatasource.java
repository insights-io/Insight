package com.meemaw.session.pages.datasource;

import com.meemaw.location.model.Location;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.useragent.model.UserAgentDTO;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import java.util.UUID;

public interface PageDatasource {

  /**
   * @param pageId String page id
   * @param sessionId String session id
   * @param deviceId device id
   * @param page CreatePageDTO page
   * @return page identity
   */
  Uni<PageIdentity> insertPage(UUID pageId, UUID sessionId, UUID deviceId, CreatePageDTO page);

  /**
   * Create page and new session linked to it.
   *
   * @param pageId page id
   * @param sessionId session id
   * @param deviceId device id
   * @param userAgent parsed user agent
   * @param location parsed location
   * @param page page create dto
   * @return page identity
   */
  Uni<PageIdentity> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      UserAgentDTO userAgent,
      Location location,
      CreatePageDTO page);

  /**
   * @param id page id
   * @param sessionId String session id
   * @param organizationId String organization id
   * @return maybe page
   */
  Uni<Optional<PageDTO>> getPage(UUID id, UUID sessionId, String organizationId);
}
