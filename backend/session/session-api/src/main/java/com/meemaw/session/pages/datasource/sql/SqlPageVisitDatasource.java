package com.meemaw.session.pages.datasource.sql;

import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.COMPILED_TIMESTAMP;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.CREATED_AT;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.DOCTYPE;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.FIELD_MAPPINGS;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.HEIGHT;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.INSERT_FIELDS;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.ORGANIZATION_ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.REFERRER;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.SCREEN_HEIGHT;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.SCREEN_WIDTH;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.SESSION_ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.TABLE;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.URL;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.WIDTH;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.location.model.Located;
import com.meemaw.session.model.CreatePageVisitDTO;
import com.meemaw.session.model.PageVisitDTO;
import com.meemaw.session.model.PageVisitSessionLink;
import com.meemaw.session.pages.datasource.PageVisitDatasource;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.rest.query.SQLGroupByQuery;
import com.meemaw.shared.sql.rest.query.SQLSearchDTO;
import com.meemaw.useragent.model.HasUserAgent;
import io.vertx.mutiny.sqlclient.Row;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Field;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlPageVisitDatasource implements PageVisitDatasource {

  @Inject ObjectMapper objectMapper;
  @Inject SqlPool sqlPool;
  @Inject SessionDatasource sessionDatasource;

  @Override
  public CompletionStage<PageVisitSessionLink> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      HasUserAgent userAgent,
      Located location,
      CreatePageVisitDTO page) {
    return sqlPool
        .beginTransaction()
        .thenCompose(
            transaction ->
                createPageAndNewSession(
                    pageId, sessionId, deviceId, userAgent, location, page, transaction));
  }

  private CompletionStage<PageVisitSessionLink> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      HasUserAgent userAgent,
      Located location,
      CreatePageVisitDTO page,
      SqlTransaction transaction) {
    String organizationId = page.getOrganizationId();
    return sessionDatasource
        .createSession(sessionId, deviceId, organizationId, location, userAgent, transaction)
        .thenCompose(
            session ->
                insertPage(pageId, session.getId(), session.getDeviceId(), page, transaction)
                    .thenCompose(
                        pageIdentity -> transaction.commit().thenApply(ignored -> pageIdentity)));
  }

  private CompletionStage<PageVisitSessionLink> insertPage(
      UUID id, UUID sessionId, UUID deviceId, CreatePageVisitDTO page, SqlTransaction transaction) {
    Query query = insertPageQuery(id, sessionId, page);
    return transaction
        .execute(query)
        .thenApply(
            rowSet ->
                PageVisitSessionLink.builder()
                    .pageVisitId(id)
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .build());
  }

  @Override
  public CompletionStage<JsonNode> count(String organizationId, SearchDTO searchDTO) {
    List<Field<?>> columns =
        SQLGroupByQuery.of(searchDTO.getGroupBy(), searchDTO.getDateTrunc())
            .selectFieldsWithCount();

    Query query =
        SQLSearchDTO.of(searchDTO)
            .query(
                sqlPool
                    .getContext()
                    .select(columns)
                    .from(TABLE)
                    .where(ORGANIZATION_ID.eq(organizationId)),
                FIELD_MAPPINGS);

    return sqlPool
        .execute(query)
        .thenApply(rows -> SQLGroupByQuery.mapRowsToJsonNode(rows, columns, objectMapper));
  }

  @Override
  public CompletionStage<PageVisitSessionLink> create(
      UUID id, UUID sessionId, UUID deviceId, CreatePageVisitDTO page) {
    Query query = insertPageQuery(id, sessionId, page);
    return sqlPool
        .execute(query)
        .thenApply(
            rowSet ->
                PageVisitSessionLink.builder()
                    .pageVisitId(id)
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .build());
  }

  private Query insertPageQuery(UUID id, UUID sessionId, CreatePageVisitDTO page) {
    return sqlPool
        .getContext()
        .insertInto(TABLE)
        .columns(INSERT_FIELDS)
        .values(
            id,
            sessionId,
            page.getOrganizationId(),
            page.getDoctype(),
            page.getUrl(),
            page.getReferrer(),
            page.getHeight(),
            page.getWidth(),
            page.getScreenHeight(),
            page.getScreenWidth(),
            page.getCompiledTs());
  }

  @Override
  public CompletionStage<Optional<PageVisitDTO>> retrieve(UUID id, String organizationId) {
    Query query =
        sqlPool
            .getContext()
            .select()
            .from(TABLE)
            .where(ID.eq(id).and(ORGANIZATION_ID.eq(organizationId)));

    return sqlPool
        .execute(query)
        .thenApply(rowSet -> rowSet.iterator().hasNext() ? map(rowSet.iterator().next()) : null)
        .thenApply(Optional::ofNullable);
  }

  private PageVisitDTO map(Row row) {
    return new PageVisitDTO(
        row.getUUID(ID.getName()),
        row.getUUID(SESSION_ID.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        row.getString(URL.getName()),
        row.getString(REFERRER.getName()),
        row.getString(DOCTYPE.getName()),
        row.getInteger(SCREEN_WIDTH.getName()),
        row.getInteger(SCREEN_HEIGHT.getName()),
        row.getInteger(WIDTH.getName()),
        row.getInteger(HEIGHT.getName()),
        row.getInteger(COMPILED_TIMESTAMP.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
