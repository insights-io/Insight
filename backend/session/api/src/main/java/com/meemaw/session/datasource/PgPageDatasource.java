package com.meemaw.session.datasource;

import com.meemaw.session.model.Page;
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

  @Inject
  PgPool pgPool;

  private static final String SELECT_LINK_DEVICE_SESSION_RAW_SQL = "SELECT session_id FROM rec.page WHERE organization = $1 AND uid = $2 AND page_start > now() - INTERVAL '30 min' ORDER BY page_start DESC LIMIT 1;";

  public Uni<Optional<UUID>> findUserSessionLink(String orgId, UUID uid) {
    Tuple values = Tuple.of(orgId, uid);
    return pgPool.preparedQuery(SELECT_LINK_DEVICE_SESSION_RAW_SQL, values)
        .map(this::extractSessionId)
        .onFailure().invoke(throwable -> {
          log.error("Failed to findDeviceSession", throwable);
          throw new DatabaseException();
        });
  }

  private Optional<UUID> extractSessionId(RowSet<Row> pgRowSet) {
    RowIterator<Row> iterator = pgRowSet.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    return Optional.of(iterator.next().getUUID(0));
  }

  private static final String INSERT_PAGE_RAW_SQL = "INSERT INTO rec.page (id, uid, session_id, organization, doctype, url, referrer, height, width, screen_height, screen_width, compiled_timestamp) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12);";

  public Uni<PageIdentity> insertPage(UUID pageId, UUID uid, UUID sessionId, Page page) {
    Tuple values = Tuple.newInstance(io.vertx.sqlclient.Tuple.of(
        pageId,
        uid,
        sessionId,
        page.getOrganization(),
        page.getDoctype(),
        page.getUrl(),
        page.getReferrer(),
        page.getHeight(),
        page.getWidth(),
        page.getScreenHeight(),
        page.getScreenWidth(),
        page.getCompiledTimestamp()
    ));

    return pgPool.preparedQuery(INSERT_PAGE_RAW_SQL, values)
        .map(rowSet -> PageIdentity.builder().pageId(pageId).sessionId(sessionId).uid(uid).build())
        .onFailure()
        .invoke(throwable -> {
          log.error("Failed to insertPage", throwable);
          throw new DatabaseException();
        });
  }

  private static final String SELECT_ACTIVE_PAGE_COUNT = "SELECT COUNT(*) FROM rec.page WHERE page_end IS NULL;";


  @Override
  public Uni<Integer> activePageCount() {
    return pgPool.preparedQuery(SELECT_ACTIVE_PAGE_COUNT)
        .map(rowSet -> rowSet.iterator().next().getInteger("count"))
        .onFailure()
        .invoke(throwable -> {
          log.error("Failed to COUNT(*) active pages", throwable);
          throw new DatabaseException();
        });
  }

}
