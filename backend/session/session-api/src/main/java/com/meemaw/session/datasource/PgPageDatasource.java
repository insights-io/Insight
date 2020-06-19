package com.meemaw.session.datasource;

import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.shared.rest.exception.DatabaseException;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PgPageDatasource implements PageDatasource {

  @Inject PgPool pgPool;
  @Inject PgSessionDatasource sessionDatasource;

  private static final String INSERT_PAGE_RAW_SQL =
      "INSERT INTO session.page (id, session_id, organization_id, doctype, url, referrer, height, width, screen_height, screen_width, compiled_timestamp) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11);";

  private static final String SELECT_PAGE_RAW_SQL =
      "SELECT * FROM session.page WHERE id=$1 AND session_id=$2 AND organization_id=$3;";

  @Override
  public Uni<PageIdentity> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      String userAgent,
      String ipAddress,
      CreatePageDTO page) {
    return pgPool
        .begin()
        .onItem()
        .produceUni(
            transaction ->
                createPageAndNewSession(
                    transaction, pageId, sessionId, deviceId, userAgent, ipAddress, page));
  }

  private Uni<PageIdentity> createPageAndNewSession(
      Transaction transaction,
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      String userAgent,
      String ipAddress,
      CreatePageDTO page) {
    return sessionDatasource
        .createSession(
            transaction, sessionId, deviceId, page.getOrganizationId(), ipAddress, userAgent)
        .onItem()
        .produceUni(
            session ->
                insertPage(transaction, pageId, session.getId(), session.getDeviceId(), page)
                    .onItem()
                    .produceUni(pageIdentity -> transaction.commit().map(ignored -> pageIdentity)));
  }

  private Uni<PageIdentity> insertPage(
      Transaction transaction, UUID id, UUID sessionId, UUID deviceId, CreatePageDTO page) {
    return transaction
        .preparedQuery(INSERT_PAGE_RAW_SQL)
        .execute(insertPageValues(id, sessionId, page))
        .map(
            rowSet ->
                PageIdentity.builder().pageId(id).sessionId(sessionId).deviceId(deviceId).build())
        .onFailure()
        .invoke(this::onInsertPageException);
  }

  @Override
  public Uni<PageIdentity> insertPage(UUID id, UUID sessionId, UUID deviceId, CreatePageDTO page) {
    return pgPool
        .preparedQuery(INSERT_PAGE_RAW_SQL)
        .execute(insertPageValues(id, sessionId, page))
        .map(
            rowSet ->
                PageIdentity.builder().pageId(id).sessionId(sessionId).deviceId(deviceId).build())
        .onFailure()
        .invoke(this::onInsertPageException);
  }

  private Tuple insertPageValues(UUID id, UUID sessionId, CreatePageDTO page) {
    return Tuple.newInstance(
        io.vertx.sqlclient.Tuple.of(
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
            page.getCompiledTs()));
  }

  private <T> T onInsertPageException(Throwable throwable) {
    log.error("Failed to insertPage", throwable);
    throw new DatabaseException(throwable);
  }

  @Override
  public Uni<Optional<PageDTO>> getPage(UUID pageId, UUID sessionId, String organizationId) {
    return pgPool
        .preparedQuery(SELECT_PAGE_RAW_SQL)
        .execute(Tuple.of(pageId, sessionId, organizationId))
        .map(
            rowSet -> {
              if (!rowSet.iterator().hasNext()) {
                return null;
              }
              Row row = rowSet.iterator().next();
              return new PageDTO(
                  row.getUUID("id"),
                  row.getUUID("session_id"),
                  row.getString("organization_id"),
                  row.getString("url"),
                  row.getString("referrer"),
                  row.getString("doctype"),
                  row.getInteger("screen_width"),
                  row.getInteger("screen_height"),
                  row.getInteger("width"),
                  row.getInteger("height"),
                  row.getInteger("compiled_timestamp"));
            })
        .map(Optional::ofNullable)
        .onFailure()
        .invoke(this::onGetPageException);
  }

  private <T> T onGetPageException(Throwable throwable) {
    log.error("Failed to get page", throwable);
    throw new DatabaseException(throwable);
  }
}
