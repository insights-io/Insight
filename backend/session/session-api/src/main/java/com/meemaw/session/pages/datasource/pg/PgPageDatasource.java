package com.meemaw.session.pages.datasource.pg;

import static com.meemaw.session.pages.datasource.pg.PageTable.COMPILED_TIMESTAMP;
import static com.meemaw.session.pages.datasource.pg.PageTable.CREATED_AT;
import static com.meemaw.session.pages.datasource.pg.PageTable.DOCTYPE;
import static com.meemaw.session.pages.datasource.pg.PageTable.HEIGHT;
import static com.meemaw.session.pages.datasource.pg.PageTable.ID;
import static com.meemaw.session.pages.datasource.pg.PageTable.INSERT_FIELDS;
import static com.meemaw.session.pages.datasource.pg.PageTable.ORGANIZATION_ID;
import static com.meemaw.session.pages.datasource.pg.PageTable.REFERRER;
import static com.meemaw.session.pages.datasource.pg.PageTable.SCREEN_HEIGHT;
import static com.meemaw.session.pages.datasource.pg.PageTable.SCREEN_WIDTH;
import static com.meemaw.session.pages.datasource.pg.PageTable.SESSION_ID;
import static com.meemaw.session.pages.datasource.pg.PageTable.TABLE;
import static com.meemaw.session.pages.datasource.pg.PageTable.URL;
import static com.meemaw.session.pages.datasource.pg.PageTable.WIDTH;

import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.session.pages.datasource.PageDatasource;
import com.meemaw.session.sessions.datasource.pg.PgSessionDatasource;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.sql.SQLContext;
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
import org.jooq.Query;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgPageDatasource implements PageDatasource {

  @Inject PgPool pgPool;
  @Inject PgSessionDatasource sessionDatasource;

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
    Query query = insertPageQuery(id, sessionId, page);
    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .map(
            rowSet ->
                PageIdentity.builder().pageId(id).sessionId(sessionId).deviceId(deviceId).build())
        .onFailure()
        .invoke(this::onInsertPageException);
  }

  @Override
  public Uni<PageIdentity> insertPage(UUID id, UUID sessionId, UUID deviceId, CreatePageDTO page) {
    Query query = insertPageQuery(id, sessionId, page);
    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .map(
            rowSet ->
                PageIdentity.builder().pageId(id).sessionId(sessionId).deviceId(deviceId).build())
        .onFailure()
        .invoke(this::onInsertPageException);
  }

  private Query insertPageQuery(UUID id, UUID sessionId, CreatePageDTO page) {
    return SQLContext.POSTGRES
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
  public Uni<Optional<PageDTO>> getPage(UUID id, UUID sessionId, String organizationId) {
    Query query =
        SQLContext.POSTGRES
            .select()
            .from(TABLE)
            .where(ID.eq(id).and(SESSION_ID.eq(sessionId).and(ORGANIZATION_ID.eq(organizationId))));

    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .map(rowSet -> rowSet.iterator().hasNext() ? map(rowSet.iterator().next()) : null)
        .map(Optional::ofNullable)
        .onFailure()
        .invoke(this::onGetPageException);
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
