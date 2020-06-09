package com.meemaw.session.datasource;

import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.shared.rest.exception.DatabaseException;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
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

  private static final String SELECT_LINK_DEVICE_SESSION_RAW_SQL =
      "SELECT session_id FROM session.page WHERE organization_id = $1 AND device_id = $2 AND page_start > now() - INTERVAL '30 min' ORDER BY page_start DESC LIMIT 1;";

  private static final String INSERT_PAGE_RAW_SQL =
      "INSERT INTO session.page (id, device_id, session_id, organization_id, doctype, url, referrer, height, width, screen_height, screen_width, compiled_timestamp) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12);";

  private static final String SELECT_ACTIVE_PAGE_COUNT =
      "SELECT COUNT(*) FROM session.page WHERE page_end IS NULL;";

  private static final String SELECT_PAGE_RAW_SQL =
      "SELECT * FROM session.page WHERE id=$1 AND session_id=$2 AND organization_id=$3;";

  @Override
  public Uni<Optional<UUID>> findUserSessionLink(String organizationId, UUID deviceId) {
    return pgPool
        .preparedQuery(SELECT_LINK_DEVICE_SESSION_RAW_SQL)
        .execute(Tuple.of(organizationId, deviceId))
        .map(this::mapSessionId)
        .onFailure()
        .invoke(this::onFindUserSessionLinkException);
  }

  private <T> T onFindUserSessionLinkException(Throwable throwable) {
    log.error("Failed to findUserSessionLinkException", throwable);
    throw new DatabaseException(throwable);
  }

  private Optional<UUID> mapSessionId(RowSet<Row> pgRowSet) {
    RowIterator<Row> iterator = pgRowSet.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    return Optional.of(iterator.next().getUUID(0));
  }

  @Override
  public Uni<PageIdentity> insertPage(
      UUID pageId, UUID deviceId, UUID sessionId, CreatePageDTO page) {
    Tuple values =
        Tuple.newInstance(
            io.vertx.sqlclient.Tuple.of(
                pageId,
                deviceId,
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

    return pgPool
        .preparedQuery(INSERT_PAGE_RAW_SQL)
        .execute(values)
        .map(
            rowSet ->
                PageIdentity.builder()
                    .pageId(pageId)
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .build())
        .onFailure()
        .invoke(this::onInsertPageException);
  }

  private <T> T onInsertPageException(Throwable throwable) {
    log.error("Failed to insertPage", throwable);
    throw new DatabaseException(throwable);
  }

  @Override
  public Uni<Integer> activePageCount() {
    return pgPool
        .preparedQuery(SELECT_ACTIVE_PAGE_COUNT)
        .execute()
        .map(rowSet -> rowSet.iterator().next().getInteger("count"))
        .onFailure()
        .invoke(this::onActivePageCountException);
  }

  private <T> T onActivePageCountException(Throwable throwable) {
    log.error("Failed to COUNT(*) active pages", throwable);
    throw new DatabaseException(throwable);
  }

  @Override
  public Uni<Optional<PageDTO>> getPage(UUID pageID, UUID sessionID, String organizationID) {
    return pgPool
        .preparedQuery(SELECT_PAGE_RAW_SQL)
        .execute(Tuple.of(pageID, sessionID, organizationID))
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
                  row.getUUID("device_id"),
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
