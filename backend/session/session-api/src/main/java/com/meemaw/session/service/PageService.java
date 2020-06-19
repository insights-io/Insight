package com.meemaw.session.service;

import com.meemaw.session.datasource.PageDatasource;
import com.meemaw.session.datasource.SessionDatasource;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.shared.logging.LoggingConstants;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class PageService {

  @Inject SessionDatasource sessionDatasource;
  @Inject PageDatasource pageDatasource;

  /**
   * Create a new page. This method is called as a first action of the tracking script to link
   * sessions.
   *
   * <p>If device id is missing, we cannot link it to any existing session thus a new session is
   * created. If device id is included, we try to link it to a existing session active in last 45
   * minutes. In case such session is not found (doesn't exist or it is more than 45 minutes old),
   * new session is created.
   *
   * @param page CreatePageDTO payload
   * @param userAgent browser user agent
   * @param ipAddress request ip address
   * @return PageIdentity
   */
  @Timed(name = "createPage", description = "A measure of how long it takes to create a page")
  public Uni<PageIdentity> createPage(CreatePageDTO page, String userAgent, String ipAddress) {
    UUID pageId = UUID.randomUUID();
    UUID deviceId = Optional.ofNullable(page.getDeviceId()).orElseGet(UUID::randomUUID);
    MDC.put(LoggingConstants.PAGE_ID, pageId.toString());
    MDC.put(LoggingConstants.DEVICE_ID, deviceId.toString());
    MDC.put(LoggingConstants.ORGANIZATION_ID, page.getOrganizationId());

    // unrecognized device; start a new session
    if (!deviceId.equals(page.getDeviceId())) {
      return createPageAndNewSession(pageId, deviceId, userAgent, ipAddress, page);
    }

    // recognized device; try to link it with an existing session
    return sessionDatasource
        .findSessionDeviceLink(page.getOrganizationId(), deviceId)
        .onItem()
        .produceUni(
            maybeSessionId -> {
              if (maybeSessionId.isEmpty()) {
                log.warn("Failed to link to an existing session");
                return createPageAndNewSession(pageId, deviceId, userAgent, ipAddress, page);
              }
              return pageDatasource.insertPage(pageId, maybeSessionId.get(), deviceId, page);
            });
  }

  private Uni<PageIdentity> createPageAndNewSession(
      UUID pageId, UUID deviceId, String userAgent, String ipAddress, CreatePageDTO page) {
    UUID sessionId = UUID.randomUUID();
    MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());
    log.info("Creating new session");
    return pageDatasource.createPageAndNewSession(
        pageId, sessionId, deviceId, userAgent, ipAddress, page);
  }

  public Uni<Optional<PageDTO>> getPage(UUID id, UUID sessionId, String organizationId) {
    return pageDatasource.getPage(id, sessionId, organizationId);
  }
}
