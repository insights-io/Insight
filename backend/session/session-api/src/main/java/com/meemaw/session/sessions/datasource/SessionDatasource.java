package com.meemaw.session.sessions.datasource;

import com.meemaw.location.model.Location;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.useragent.model.UserAgentDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Transaction;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.opentracing.Traced;

public interface SessionDatasource {

  @Traced
  Uni<Optional<UUID>> findSessionDeviceLink(String organizationId, UUID deviceId);

  @Traced
  Uni<SessionDTO> createSession(
      Transaction transaction,
      UUID sessionId,
      UUID deviceId,
      String organizationId,
      Location location,
      UserAgentDTO userAgent);

  @Traced
  Uni<Optional<SessionDTO>> getSession(UUID id, String organizationId);

  @Traced
  Uni<Collection<SessionDTO>> getSessions(String organizationId, SearchDTO searchDTO);
}
