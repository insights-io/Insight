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

  Uni<PageIdentity> insertPage(UUID pageId, UUID sessionId, UUID deviceId, CreatePageDTO page);

  Uni<PageIdentity> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      UserAgentDTO userAgent,
      Location location,
      CreatePageDTO page);

  Uni<Optional<PageDTO>> getPage(UUID id, UUID sessionId, String organizationId);
}
