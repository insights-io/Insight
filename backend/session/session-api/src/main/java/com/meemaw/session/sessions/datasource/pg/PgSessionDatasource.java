package com.meemaw.session.sessions.datasource.pg;

import static com.meemaw.session.sessions.datasource.pg.SessionTable.CREATED_AT;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.DEVICE_ID;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.FIELDS;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.FIELD_MAPPINGS;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.ID;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.INSERT_FIELDS;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.IP_ADDRESS;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.ORGANIZATION_ID;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.TABLE;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.USER_AGENT;

import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.query.sql.SQLSearchDTO;
import com.meemaw.shared.sql.SQLContext;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgSessionDatasource implements SessionDatasource {

  @Inject PgPool pgPool;

  @Override
  public Uni<Optional<UUID>> findSessionDeviceLink(String organizationId, UUID deviceId) {
    Query query =
        SQLContext.POSTGRES
            .select(ID)
            .from(TABLE)
            .where(ORGANIZATION_ID.eq(organizationId).and(DEVICE_ID.eq(deviceId)));

    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .map(this::mapSessionId)
        .onFailure()
        .invoke(this::onFindUserSessionLinkException);
  }

  private Optional<UUID> mapSessionId(RowSet<Row> pgRowSet) {
    RowIterator<Row> iterator = pgRowSet.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    return Optional.of(iterator.next().getUUID(0));
  }

  private <T> T onFindUserSessionLinkException(Throwable throwable) {
    log.error("Failed to findUserSessionLinkException", throwable);
    throw new DatabaseException(throwable);
  }

  @Override
  public Uni<SessionDTO> createSession(
      Transaction transaction,
      UUID id,
      UUID deviceId,
      String organizationId,
      String ipAddress,
      String userAgent) {
    Query query =
        SQLContext.POSTGRES
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(id, deviceId, organizationId, ipAddress, userAgent)
            .returning(FIELDS);

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .map(rowSet -> mapSession(rowSet.iterator().next()))
        .onFailure()
        .invoke(this::onCreateSessionException);
  }

  @Override
  public Uni<Optional<SessionDTO>> getSession(UUID id, String organizationId) {
    Query query =
        SQLContext.POSTGRES
            .select()
            .from(TABLE)
            .where(ID.eq(id).and(ORGANIZATION_ID.eq(organizationId)));

    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .map(rowSet -> rowSet.iterator().hasNext() ? mapSession(rowSet.iterator().next()) : null)
        .map(Optional::ofNullable);
  }

  @Override
  public Uni<Collection<SessionDTO>> getSessions(String organizationId, SearchDTO searchDTO) {
    Query query =
        SQLSearchDTO.of(searchDTO)
            .apply(
                SQLContext.POSTGRES.selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId)),
                FIELD_MAPPINGS);

    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .map(this::mapSessions);
  }

  private List<SessionDTO> mapSessions(RowSet<Row> rowSet) {
    List<SessionDTO> collection = new ArrayList<>(rowSet.size());
    for (Row row : rowSet) {
      collection.add(mapSession(row));
    }
    return collection;
  }

  private SessionDTO mapSession(Row row) {
    return new SessionDTO(
        row.getUUID(ID.getName()),
        row.getUUID(DEVICE_ID.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        row.getString(IP_ADDRESS.getName()),
        row.getString(USER_AGENT.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }

  private <T> T onCreateSessionException(Throwable throwable) {
    log.error("Failed to create session", throwable);
    throw new DatabaseException(throwable);
  }
}
