package com.meemaw.session.sessions.datasource;

import com.meemaw.location.model.Location;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.useragent.model.UserAgentDTO;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SessionDatasource {

  CompletionStage<Optional<UUID>> findSessionDeviceLink(String organizationId, UUID deviceId);

  CompletionStage<SessionDTO> createSession(
      UUID sessionId,
      UUID deviceId,
      String organizationId,
      Location location,
      UserAgentDTO userAgent,
      SqlTransaction transaction);

  CompletionStage<Optional<SessionDTO>> getSession(UUID id, String organizationId);

  CompletionStage<Collection<SessionDTO>> getSessions(String organizationId, SearchDTO searchDTO);
}
