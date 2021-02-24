package com.rebrowse.session.pages.datasource.sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.location.model.Located;
import com.rebrowse.session.model.PageVisitCreateParams;
import com.rebrowse.session.model.PageVisitDTO;
import com.rebrowse.session.model.PageVisitSessionLink;
import com.rebrowse.session.pages.datasource.PageVisitDatasource;
import com.rebrowse.session.sessions.datasource.SessionDatasource;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.context.URIUtils;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.datasource.AbstractSqlDatasource;
import com.rebrowse.shared.sql.rest.query.SQLGroupByQuery;
import com.rebrowse.shared.sql.rest.query.SQLSearchDTO;
import com.rebrowse.useragent.model.HasUserAgent;
import io.vertx.mutiny.sqlclient.Row;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Field;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlPageVisitDatasource extends AbstractSqlDatasource<PageVisitDTO>
    implements PageVisitDatasource {

  @Inject ObjectMapper objectMapper;
  @Inject SqlPool sqlPool;
  @Inject SessionDatasource sessionDatasource;

  public static PageVisitDTO map(Row row) {
    return new PageVisitDTO(
        row.getUUID(SqlPageVisitTable.ID.getName()),
        row.getUUID(SqlPageVisitTable.SESSION_ID.getName()),
        row.getString(SqlPageVisitTable.ORGANIZATION_ID.getName()),
        row.getString(SqlPageVisitTable.DOCTYPE.getName()),
        RequestUtils.sneakyUrl(row.getString(SqlPageVisitTable.ORIGIN.getName())),
        row.getString(SqlPageVisitTable.PATH.getName()),
        row.getString(SqlPageVisitTable.REFERRER.getName()),
        row.getInteger(SqlPageVisitTable.SCREEN_WIDTH.getName()),
        row.getInteger(SqlPageVisitTable.SCREEN_HEIGHT.getName()),
        row.getInteger(SqlPageVisitTable.WIDTH.getName()),
        row.getInteger(SqlPageVisitTable.HEIGHT.getName()),
        row.getInteger(SqlPageVisitTable.COMPILED_TIMESTAMP.getName()),
        row.getOffsetDateTime(SqlPageVisitTable.CREATED_AT.getName()));
  }

  @Override
  public CompletionStage<PageVisitSessionLink> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      HasUserAgent userAgent,
      Located location,
      PageVisitCreateParams createParams) {
    return sqlPool
        .beginTransaction()
        .thenCompose(
            transaction ->
                createPageAndNewSession(
                        pageId, sessionId, deviceId, userAgent, location, createParams, transaction)
                    .thenCompose(link -> transaction.commit().thenApply(i -> link)));
  }

  private CompletionStage<PageVisitSessionLink> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      HasUserAgent userAgent,
      Located location,
      PageVisitCreateParams page,
      SqlTransaction transaction) {
    return sessionDatasource
        .create(sessionId, deviceId, page.getOrganizationId(), location, userAgent, transaction)
        .thenCompose(
            session ->
                insertPage(pageId, session.getId(), session.getDeviceId(), page, transaction));
  }

  private CompletionStage<PageVisitSessionLink> insertPage(
      UUID id,
      UUID sessionId,
      UUID deviceId,
      PageVisitCreateParams page,
      SqlTransaction transaction) {
    return transaction
        .execute(insertPageQuery(id, sessionId, page))
        .thenApply(
            r ->
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
                    .from(SqlPageVisitTable.TABLE)
                    .where(SqlPageVisitTable.ORGANIZATION_ID.eq(organizationId)),
                SqlPageVisitTable.FIELD_MAPPINGS);

    return sqlPool
        .execute(query)
        .thenApply(rows -> SQLGroupByQuery.mapRowsToJsonNode(rows, columns, objectMapper));
  }

  @Override
  public CompletionStage<PageVisitSessionLink> create(
      UUID id, UUID sessionId, UUID deviceId, PageVisitCreateParams page) {
    return sqlPool
        .execute(insertPageQuery(id, sessionId, page))
        .thenApply(
            rowSet ->
                PageVisitSessionLink.builder()
                    .pageVisitId(id)
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .build());
  }

  @Override
  public CompletionStage<PageVisitSessionLink> create(
      UUID pageVisitId,
      UUID sessionId,
      UUID deviceId,
      PageVisitCreateParams page,
      SqlTransaction transaction) {
    return transaction
        .execute(insertPageQuery(pageVisitId, sessionId, page))
        .thenApply(
            rowSet ->
                PageVisitSessionLink.builder()
                    .pageVisitId(pageVisitId)
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .build());
  }

  @SneakyThrows
  private Query insertPageQuery(UUID pageVisitId, UUID sessionId, PageVisitCreateParams page) {
    URI origin = URIUtils.parseOrigin(page.getHref());
    String path = URIUtils.removeTrailingSlash(page.getHref().getPath());

    return sqlPool
        .getContext()
        .insertInto(SqlPageVisitTable.TABLE)
        .columns(SqlPageVisitTable.INSERT_FIELDS)
        .values(
            pageVisitId,
            sessionId,
            page.getOrganizationId(),
            page.getDoctype(),
            origin.toString(),
            path,
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
            .from(SqlPageVisitTable.TABLE)
            .where(
                SqlPageVisitTable.ID
                    .eq(id)
                    .and(SqlPageVisitTable.ORGANIZATION_ID.eq(organizationId)));

    return sqlPool.execute(query).thenApply(this::findOne);
  }

  @Override
  public PageVisitDTO fromSql(Row row) {
    return SqlPageVisitDatasource.map(row);
  }
}
