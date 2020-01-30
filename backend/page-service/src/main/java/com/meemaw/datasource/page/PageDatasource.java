package com.meemaw.datasource.page;

import com.meemaw.model.page.Page;
import com.meemaw.model.page.PageIdentityDTO;
import com.meemaw.model.page.PageIdentityDTOBuilder;
import com.meemaw.rest.exception.DatabaseException;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class PageDatasource {

    private static final Logger log = LoggerFactory.getLogger(PageDatasource.class);

    private static final String SELECT_LINK_DEVICE_SESSION_RAW_SQL = "SELECT session_id " +
            "FROM rec.page " +
            "WHERE organization = $ 1 AND uid = $2 AND page_start > now() - INTERVAL '30 min' " +
            "ORDER BY page_start DESC LIMIT 1;";


    private static final String INSERT_PAGE_RAW_SQL = "INSERT INTO rec.page " +
            "(id, uid, session_id, organization, doctype, url, referrer, height, width, screen_height, screen_width, compiled_timestamp) " +
            "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12);";


    @Inject
    PgPool pgPool;


    /**
     * @param organization
     * @param uid
     * @return optionally linked sessionId that has been active in the last 30 minutes
     */
    public CompletionStage<UUID> findDeviceSession(String organization, UUID uid) {
        Tuple values = Tuple.of(organization, uid);
        return pgPool.preparedQuery(SELECT_LINK_DEVICE_SESSION_RAW_SQL, values)
                .thenApply(pgRowSet -> pgRowSet.iterator().next().getUUID(0))
                .exceptionally(throwable -> {
                    log.error("Failed to findDeviceSession", throwable);
                    throw new DatabaseException();
                });
    }


    /**
     * @param pageId
     * @param uid
     * @param sessionId
     * @param page
     * @return
     */
    public CompletionStage<PageIdentityDTO> insertPage(UUID pageId, UUID uid, UUID sessionId, Page page) {
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
                .thenApply(_pgRowSet -> new PageIdentityDTOBuilder().setPageId(pageId).setSessionId(sessionId).setUid(uid).build())
                .exceptionally(throwable -> {
                    log.error("Failed to insertPage", throwable);
                    throw new DatabaseException();
                });
    }
}
