package com.meemaw.session.pages.datasource.sql;

import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.COMPILED_TIMESTAMP;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.CREATED_AT;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.DOCTYPE;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.FIELD_MAPPINGS;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.HEIGHT;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.INSERT_FIELDS;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.ORGANIZATION_ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.ORIGIN;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.PATH;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.REFERRER;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.SCREEN_HEIGHT;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.SCREEN_WIDTH;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.SESSION_ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.TABLE;
import static com.meemaw.session.pages.datasource.sql.SqlPageVisitTable.WIDTH;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.location.model.Located;
import com.meemaw.session.model.PageVisitCreateParams;
import com.meemaw.session.model.PageVisitDTO;
import com.meemaw.session.model.PageVisitSessionLink;
import com.meemaw.session.pages.datasource.PageVisitDatasource;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.datasource.AbstractSqlDatasource;
import com.meemaw.shared.sql.rest.query.SQLGroupByQuery;
import com.meemaw.shared.sql.rest.query.SQLSearchDTO;
import com.meemaw.useragent.model.HasUserAgent;
import io.vertx.mutiny.sqlclient.Row;
import java.net.URL;
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
public class SqlPageVisitDatasource extends AbstractSqlDatasource<PageVisitDTO>
    implements PageVisitDatasource {

  @Inject ObjectMapper objectMapper;
  @Inject SqlPool sqlPool;
  @Inject SessionDatasource sessionDatasource;

  public static PageVisitDTO map(Row row) {
    return new PageVisitDTO(
        row.getUUID(ID.getName()),
        row.getUUID(SESSION_ID.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        row.getString(DOCTYPE.getName()),
        RequestUtils.sneakyUrl(row.getString(ORIGIN.getName())),
        row.getString(PATH.getName()),
        row.getString(REFERRER.getName()),
        row.getInteger(SCREEN_WIDTH.getName()),
        row.getInteger(SCREEN_HEIGHT.getName()),
        row.getInteger(WIDTH.getName()),
        row.getInteger(HEIGHT.getName()),
        row.getInteger(COMPILED_TIMESTAMP.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
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
                    .from(TABLE)
                    .where(ORGANIZATION_ID.eq(organizationId)),
                FIELD_MAPPINGS);

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

  private Query insertPageQuery(UUID pageVisitId, UUID sessionId, PageVisitCreateParams page) {
    URL origin = RequestUtils.parseOrigin(page.getHref());
    String path = RequestUtils.removeTrailingSlash(page.getHref().getPath());
    return sqlPool
        .getContext()
        .insertInto(TABLE)
        .columns(INSERT_FIELDS)
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
            .from(TABLE)
            .where(ID.eq(id).and(ORGANIZATION_ID.eq(organizationId)));

    return sqlPool.execute(query).thenApply(this::findOne);
  }

  @Override
  public PageVisitDTO fromSql(Row row) {
    return SqlPageVisitDatasource.map(row);
  }
}
