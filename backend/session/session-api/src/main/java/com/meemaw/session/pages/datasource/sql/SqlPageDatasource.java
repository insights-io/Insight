package com.meemaw.session.pages.datasource.sql;

import static com.meemaw.session.pages.datasource.sql.SqlPageTable.*;

import io.vertx.mutiny.sqlclient.Row;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;

import com.meemaw.location.model.Location;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.session.pages.datasource.PageDatasource;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.useragent.model.UserAgentDTO;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
        .beginTransaction()
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
        .execute(query)
        .thenApply(
            rowSet ->
                PageIdentity.builder().pageId(id).sessionId(sessionId).deviceId(deviceId).build());
  }

  @Override
  public CompletionStage<PageIdentity> insertPage(
      UUID id, UUID sessionId, UUID deviceId, CreatePageDTO page) {
    Query query = insertPageQuery(id, sessionId, page);
    return sqlPool
        .execute(query)
        .thenApply(
            rowSet ->
                PageIdentity.builder().pageId(id).sessionId(sessionId).deviceId(deviceId).build());
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
        .execute(query)
        .thenApply(rowSet -> rowSet.iterator().hasNext() ? map(rowSet.iterator().next()) : null)
        .thenApply(Optional::ofNullable);
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
}
