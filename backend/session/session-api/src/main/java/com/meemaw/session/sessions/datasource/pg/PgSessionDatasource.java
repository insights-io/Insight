package com.meemaw.session.sessions.datasource.pg;

import static com.meemaw.session.sessions.datasource.pg.SessionTable.CREATED_AT;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.DEVICE_ID;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.FIELDS;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.FIELD_MAPPINGS;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.ID;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.INSERT_FIELDS;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.LOCATION;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.ORGANIZATION_ID;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.TABLE;
import static com.meemaw.session.sessions.datasource.pg.SessionTable.USER_AGENT;
import static org.jooq.impl.DSL.condition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.location.model.Location;
import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.sql.SQLContext;
import com.meemaw.shared.sql.rest.query.SQLSearchDTO;
import com.meemaw.useragent.model.UserAgentDTO;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgSessionDatasource implements SessionDatasource {

  @Inject PgPool pgPool;
  @Inject ObjectMapper objectMapper;

  @Override
  public Uni<Optional<UUID>> findSessionDeviceLink(String organizationId, UUID deviceId) {
    Query query =
        SQLContext.POSTGRES
            .select(ID)
            .from(TABLE)
            .where(
                ORGANIZATION_ID
                    .eq(organizationId)
                    .and(DEVICE_ID.eq(deviceId))
                    .and(condition("created_at > now() - INTERVAL '30 min'")))
            .orderBy(CREATED_AT.desc())
            .limit(1);

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

  @SneakyThrows
  @Override
  public Uni<SessionDTO> createSession(
      Transaction transaction,
      UUID id,
      UUID deviceId,
      String organizationId,
      Location location,
      UserAgentDTO userAgent) {
    Query query =
        SQLContext.POSTGRES
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                id,
                deviceId,
                organizationId,
                objectMapper.writeValueAsString(location),
                objectMapper.writeValueAsString(userAgent))
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

  @SneakyThrows
  private SessionDTO mapSession(Row row) {
    return new SessionDTO(
        row.getUUID(ID.getName()),
        row.getUUID(DEVICE_ID.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        objectMapper.readValue(row.getString(LOCATION.getName()), LocationDTO.class),
        objectMapper.readValue(row.getString(USER_AGENT.getName()), UserAgentDTO.class),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }

  private <T> T onCreateSessionException(Throwable throwable) {
    log.error("Failed to create session", throwable);
    throw new DatabaseException(throwable);
  }
}
