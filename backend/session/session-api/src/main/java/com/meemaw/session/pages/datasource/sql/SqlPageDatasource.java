package com.meemaw.session.pages.datasource.sql;

import static com.meemaw.session.pages.datasource.sql.SqlPageTable.COMPILED_TIMESTAMP;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.CREATED_AT;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.DOCTYPE;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.HEIGHT;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.INSERT_FIELDS;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.ORGANIZATION_ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.REFERRER;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.SCREEN_HEIGHT;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.SCREEN_WIDTH;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.SESSION_ID;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.TABLE;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.URL;
import static com.meemaw.session.pages.datasource.sql.SqlPageTable.WIDTH;

import com.meemaw.location.model.Location;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.session.pages.datasource.PageDatasource;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.useragent.model.UserAgentDTO;
import io.vertx.mutiny.sqlclient.Row;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlPageDatasource implements PageDatasource {

  @Inject SqlPool sqlPool;
  @Inject SessionDatasource sessionDatasource;

  @Override
  public CompletionStage<PageIdentity> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      UserAgentDTO userAgent,
      Location location,
      CreatePageDTO page) {
    return sqlPool
        .begin()
        .thenCompose(
            transaction ->
                createPageAndNewSession(
                    pageId, sessionId, deviceId, userAgent, location, page, transaction));
  }

  private CompletionStage<PageIdentity> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      UserAgentDTO userAgent,
      Location location,
      CreatePageDTO page,
      SqlTransaction transaction) {

    return sessionDatasource
        .createSession(
            sessionId, deviceId, page.getOrganizationId(), location, userAgent, transaction)
        .thenCompose(
            session ->
                insertPage(pageId, session.getId(), session.getDeviceId(), page, transaction)
                    .thenCompose(
                        pageIdentity -> transaction.commit().thenApply(ignored -> pageIdentity)));
  }

  private CompletionStage<PageIdentity> insertPage(
      UUID id, UUID sessionId, UUID deviceId, CreatePageDTO page, SqlTransaction transaction) {
    Query query = insertPageQuery(id, sessionId, page);
    return transaction
        .query(query)
        .thenApply(
            rowSet ->
                PageIdentity.builder().pageId(id).sessionId(sessionId).deviceId(deviceId).build())
        .exceptionally(this::onInsertPageException);
  }

  @Override
  public CompletionStage<PageIdentity> insertPage(
      UUID id, UUID sessionId, UUID deviceId, CreatePageDTO page) {
    Query query = insertPageQuery(id, sessionId, page);
    return sqlPool
        .query(query)
        .thenApply(
            rowSet ->
                PageIdentity.builder().pageId(id).sessionId(sessionId).deviceId(deviceId).build())
        .exceptionally(this::onInsertPageException);
  }

  private Query insertPageQuery(UUID id, UUID sessionId, CreatePageDTO page) {
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

  private <T> T onInsertPageException(Throwable throwable) {
    log.error("Failed to insertPage", throwable);
    throw new DatabaseException(throwable);
  }

  @Override
  public CompletionStage<Optional<PageDTO>> getPage(
      UUID id, UUID sessionId, String organizationId) {
    Query query =
        sqlPool
            .getContext()
            .select()
            .from(TABLE)
            .where(ID.eq(id).and(SESSION_ID.eq(sessionId).and(ORGANIZATION_ID.eq(organizationId))));

    return sqlPool
        .query(query)
        .thenApply(rowSet -> rowSet.iterator().hasNext() ? map(rowSet.iterator().next()) : null)
        .thenApply(Optional::ofNullable)
        .exceptionally(this::onGetPageException);
  }

  private PageDTO map(Row row) {
    return new PageDTO(
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

  private <T> T onGetPageException(Throwable throwable) {
    log.error("Failed to get page", throwable);
    throw new DatabaseException(throwable);
  }
}
