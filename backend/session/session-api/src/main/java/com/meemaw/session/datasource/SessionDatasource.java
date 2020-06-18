package com.meemaw.session.datasource;

import com.meemaw.session.model.SessionDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Transaction;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SessionDatasource {

  /**
   * @param organizationId String organization id
   * @param deviceId String device id
   * @return optionally linked sessionID that has been active in the last 30 minutes
   */
  Uni<Optional<UUID>> findSessionDeviceLink(String organizationId, UUID deviceId);

  /**
   * @param transaction current transaction
   * @param deviceId id of the device
   * @param organizationId id of the organization
   * @param ipAddress ip address of the incoming session request
   * @param userAgent browser's user agent
   * @return newly created session
   */
  Uni<SessionDTO> createSession(
      Transaction transaction,
      UUID sessionId,
      UUID deviceId,
      String organizationId,
      String ipAddress,
      String userAgent);

  /**
   * Get session if it exists.
   *
   * @param id session id
   * @param organizationId organization id
   * @return maybe session
   */
  Uni<Optional<SessionDTO>> getSession(UUID id, String organizationId);

  /**
   * List sessions.
   *
   * @param organizationId organization id
   * @return collection of sessions
   */
  Uni<Collection<SessionDTO>> getSessions(String organizationId);
}
