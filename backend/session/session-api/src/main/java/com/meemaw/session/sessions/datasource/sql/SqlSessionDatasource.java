package com.meemaw.session.sessions.datasource.sql;

import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.CREATED_AT;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.DEVICE_ID;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.FIELDS;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.FIELD_MAPPINGS;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.ID;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.INSERT_FIELDS;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.LOCATION;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.ORGANIZATION_ID;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.TABLE;
import static com.meemaw.session.sessions.datasource.sql.SqlSessionTable.USER_AGENT;
import static com.meemaw.shared.sql.rest.query.SQLFilterExpression.sqlSelectField;
import static org.jooq.impl.DSL.condition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.location.model.Location;
import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.rest.query.SQLGroupByQuery;
import com.meemaw.shared.sql.rest.query.SQLSearchDTO;
import com.meemaw.useragent.model.UserAgentDTO;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Field;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlSessionDatasource implements SessionDatasource {

  @Inject SqlPool sqlPool;
  @Inject ObjectMapper objectMapper;

  @Override
  public CompletionStage<Optional<UUID>> findSessionDeviceLink(
      String organizationId, UUID deviceId) {
    Query query =
        sqlPool
            .getContext()
            .select(ID)
            .from(TABLE)
            .where(
                ORGANIZATION_ID
                    .eq(organizationId)
                    .and(DEVICE_ID.eq(deviceId))
                    .and(condition("created_at > now() - INTERVAL '30 min'")))
            .orderBy(CREATED_AT.desc())
            .limit(1);

    return sqlPool.execute(query).thenApply(this::mapSessionId);
  }

  private Optional<UUID> mapSessionId(RowSet<Row> rows) {
    RowIterator<Row> iterator = rows.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    return Optional.of(iterator.next().getUUID(0));
  }

  @Override
  public CompletionStage<JsonNode> count(String organizationId, SearchDTO searchDTO) {
    SQLGroupByQuery sqlGroupByQuery = SQLGroupByQuery.of(searchDTO.getGroupBy());

    Query query =
        SQLSearchDTO.of(searchDTO)
            .apply(
                sqlPool
                    .getContext()
                    .select(sqlGroupByQuery.fieldsWithCount())
                    .from(TABLE)
                    .where(ORGANIZATION_ID.eq(organizationId)),
                FIELD_MAPPINGS);

    return sqlPool.execute(query).thenApply(rows -> sqlGroupByQuery.asJsonNode(rows, objectMapper));
  }

  @Override
  public CompletionStage<Collection<String>> distinct(
      Collection<String> on, String organizationId, SearchDTO searchDTO) {
    List<Field<?>> fields =
        on.stream().map(f -> sqlSelectField(f, String.class)).collect(Collectors.toList());

    Query query =
        SQLSearchDTO.of(searchDTO)
            .apply(
                sqlPool
                    .getContext()
                    .selectDistinct(fields)
                    .from(TABLE)
                    .where(ORGANIZATION_ID.eq(organizationId)),
                FIELD_MAPPINGS);

    return sqlPool
        .execute(query)
        .thenApply(
            rows -> {
              Collection<String> values = new ArrayList<>(rows.size());
              for (Row row : rows) {
                values.add(row.getString(0));
              }
              return values;
            });
  }

  @Override
  public CompletionStage<SessionDTO> createSession(
      UUID id,
      UUID deviceId,
      String organizationId,
      Location location,
      UserAgentDTO userAgent,
      SqlTransaction transaction) {

    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                id,
                deviceId,
                organizationId,
                JsonObject.mapFrom(location),
                JsonObject.mapFrom(userAgent))
            .returning(FIELDS);

    return transaction.execute(query).thenApply(rowSet -> mapSession(rowSet.iterator().next()));
  }

  @Override
  public CompletionStage<Optional<SessionDTO>> getSession(UUID id, String organizationId) {
    Query query =
        sqlPool
            .getContext()
            .select()
            .from(TABLE)
            .where(ID.eq(id).and(ORGANIZATION_ID.eq(organizationId)));

    return sqlPool
        .execute(query)
        .thenApply(
            rowSet -> rowSet.iterator().hasNext() ? mapSession(rowSet.iterator().next()) : null)
        .thenApply(Optional::ofNullable);
  }

  @Override
  public CompletionStage<Collection<SessionDTO>> getSessions(
      String organizationId, SearchDTO searchDTO) {
    Query query =
        SQLSearchDTO.of(searchDTO)
            .apply(
                sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId)),
                FIELD_MAPPINGS);

    return sqlPool.execute(query).thenApply(this::mapSessions);
  }

  private List<SessionDTO> mapSessions(RowSet<Row> rowSet) {
    List<SessionDTO> collection = new ArrayList<>(rowSet.size());
    for (Row row : rowSet) {
      collection.add(mapSession(row));
    }
    return collection;
  }

  private SessionDTO mapSession(Row row) {
    JsonObject location = (JsonObject) row.getValue(LOCATION.getName());
    JsonObject userAgent = (JsonObject) row.getValue(USER_AGENT.getName());

    return new SessionDTO(
        row.getUUID(ID.getName()),
        row.getUUID(DEVICE_ID.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        location.mapTo(LocationDTO.class),
        userAgent.mapTo(UserAgentDTO.class),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
