package com.meemaw.session.datasource;

import com.meemaw.session.model.SessionDTO;
import com.meemaw.shared.rest.exception.DatabaseException;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PgSessionDatasource implements SessionDatasource {

  @Inject PgPool pgPool;

  private static final String SELECT_SESSION_DEVICE_LINK_RAW_SQL =
      "SELECT id FROM session.session WHERE organization_id = $1 AND device_id = $2 AND created_at > now() - INTERVAL '30 min' ORDER BY created_at DESC LIMIT 1;";

  private static final String INSERT_SESSION_RAW_SQL =
      "INSERT INTO session.session (id, device_id, organization_id, ip_address, user_agent) VALUES($1, $2, $3, $4, $5) RETURNING *;";

  private static final String SELECT_SESSION_RAW_SQL =
      "SELECT * FROM session.session WHERE id=$1 AND organization_id=$2;";

  @Override
  public Uni<Optional<UUID>> findSessionDeviceLink(String organizationId, UUID deviceId) {
    return pgPool
        .preparedQuery(SELECT_SESSION_DEVICE_LINK_RAW_SQL)
        .execute(Tuple.of(organizationId, deviceId))
        .map(this::mapSessionId)
        .onFailure()
        .invoke(this::onFindUserSessionLinkException);
  }

  private <T> T onFindUserSessionLinkException(Throwable throwable) {
    log.error("Failed to findUserSessionLinkException", throwable);
    throw new DatabaseException(throwable);
  }

  private Optional<UUID> mapSessionId(RowSet<Row> pgRowSet) {
    RowIterator<Row> iterator = pgRowSet.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    return Optional.of(iterator.next().getUUID(0));
  }

  @Override
  public Uni<SessionDTO> createSession(
      Transaction transaction,
      UUID sessionId,
      UUID deviceId,
      String organizationId,
      String ipAddress,
      String userAgent) {
    return transaction
        .preparedQuery(INSERT_SESSION_RAW_SQL)
        .execute(Tuple.of(sessionId, deviceId, organizationId, ipAddress, userAgent))
        .map(
            rowSet -> {
              Row row = rowSet.iterator().next();
              return new SessionDTO(
                  row.getUUID("id"),
                  row.getUUID("device_id"),
                  row.getString("organization_id"),
                  row.getString("ip_address"),
                  row.getString("user_agent"),
                  row.getOffsetDateTime("created_at"));
            })
        .onFailure()
        .invoke(this::onCreateSessionException);
  }

  @Override
  public Uni<Optional<SessionDTO>> getSession(UUID id, String organizationId) {
    return pgPool
        .preparedQuery(SELECT_SESSION_RAW_SQL)
        .execute(Tuple.of(id, organizationId))
        .map(
            rowSet -> {
              if (!rowSet.iterator().hasNext()) {
                return null;
              }
              Row row = rowSet.iterator().next();
              return new SessionDTO(
                  row.getUUID("id"),
                  row.getUUID("device_id"),
                  row.getString("organization_id"),
                  row.getString("ip_address"),
                  row.getString("user_agent"),
                  row.getOffsetDateTime("created_at"));
            })
        .map(Optional::ofNullable);
  }

  private <T> T onCreateSessionException(Throwable throwable) {
    log.error("Failed to create session", throwable);
    throw new DatabaseException(throwable);
  }
}
