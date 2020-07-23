package com.meemaw.session.pages.service;

import com.meemaw.location.model.LocationDTO;
import com.meemaw.session.location.service.LocationService;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.session.pages.datasource.PageDatasource;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.session.useragent.service.UserAgentService;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.useragent.model.UserAgentDTO;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class PageService {

  @Inject UserAgentService userAgentService;
  @Inject LocationService locationService;
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
  @Traced
  public Uni<PageIdentity> createPage(CreatePageDTO page, String userAgent, String ipAddress) {
    UUID pageId = UUID.randomUUID();
    UUID deviceId = Optional.ofNullable(page.getDeviceId()).orElseGet(UUID::randomUUID);
    MDC.put(LoggingConstants.PAGE_ID, pageId.toString());
    MDC.put(LoggingConstants.DEVICE_ID, deviceId.toString());
    MDC.put(LoggingConstants.ORGANIZATION_ID, page.getOrganizationId());

    // unrecognized device; start a new session
    if (!deviceId.equals(page.getDeviceId())) {
      log.debug("[SESSION]: Unrecognized device -- starting a new session");
      return createPageAndNewSession(pageId, deviceId, userAgent, ipAddress, page);
    }

    // recognized device; try to link it with an existing session
    return sessionDatasource
        .findSessionDeviceLink(page.getOrganizationId(), deviceId)
        .onItem()
        .produceUni(
            maybeSessionId -> {
              if (maybeSessionId.isEmpty()) {
                log.debug(
                    "[SESSION]: Failed to link to an existing session -- starting new session");
                return createPageAndNewSession(pageId, deviceId, userAgent, ipAddress, page);
              }
              MDC.put(LoggingConstants.SESSION_ID, maybeSessionId.get().toString());
              log.info("[SESSION]: Linking page to an existing session");
              return pageDatasource.insertPage(pageId, maybeSessionId.get(), deviceId, page);
            });
  }

  @Traced
  private Uni<PageIdentity> createPageAndNewSession(
      UUID pageId, UUID deviceId, String userAgentString, String ipAddress, CreatePageDTO page) {
    UUID sessionId = UUID.randomUUID();
    MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());
    log.info("[SESSION]: Creating new session");

    UserAgentDTO userAgent = userAgentService.parse(userAgentString);
    LocationDTO location = locationService.lookupByIp(ipAddress);

    return pageDatasource.createPageAndNewSession(
        pageId, sessionId, deviceId, userAgent, location, page);
  }

  public Uni<Optional<PageDTO>> getPage(UUID id, UUID sessionId, String organizationId) {
    MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    MDC.put(LoggingConstants.PAGE_ID, id.toString());
    log.debug("[SESSION]: get page by id");
    return pageDatasource.getPage(id, sessionId, organizationId);
  }
}
