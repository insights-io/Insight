package com.meemaw.session.pages.service;

import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.organization.resource.v1.OrganizationResource;
import com.meemaw.location.model.Location;
import com.meemaw.session.location.service.LocationService;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.model.PageDTO;
import com.meemaw.session.model.PageIdentity;
import com.meemaw.session.pages.datasource.PageDatasource;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.session.useragent.service.UserAgentService;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.useragent.model.UserAgentDTO;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.GenericType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class PageService {

  @Inject UserAgentService userAgentService;
  @Inject LocationService locationService;
  @Inject SessionDatasource sessionDatasource;
  @Inject PageDatasource pageDatasource;
  @Inject @RestClient OrganizationResource organizationResource;

  /**
   * Create a new page. This method is called as a first action of the tracking script to link
   * sessions.
   *
   * <p>1. Check if the organization exists. 2. Increment the session usage counter 3. Check if
   * organization is on free plan and if it has exceeded the free usage 4. If device id is missing,
   * we cannot link it to any existing session thus a new session is created. If device id is
   * included, we try to link it to a existing session active in last 45 minutes. In case such
   * session is not found (doesn't exist or it is more than 45 minutes old), new session is created.
   *
   * @param page CreatePageDTO payload
   * @param userAgentString as obtained from User-Agent header
   * @param ipAddress request ip address
   * @return PageIdentity
   */
  @Timed(name = "createPage", description = "A measure of how long it takes to create a page")
  @Traced
  public CompletionStage<PageIdentity> createPage(
      CreatePageDTO page, String userAgentString, String ipAddress) {
    UserAgentDTO userAgent = userAgentService.parse(userAgentString);
    if (userAgent.isRobot() || userAgent.isHacker()) {
      log.debug(
          "[SESSION]: Create page attempt by robot ip={} userAgent={}", ipAddress, userAgentString);
      throw Boom.badRequest().message("You're a robot").exception();
    }

    String organizationId = page.getOrganizationId();
    UUID pageId = UUID.randomUUID();
    UUID deviceId = Optional.ofNullable(page.getDeviceId()).orElseGet(UUID::randomUUID);
    MDC.put(LoggingConstants.PAGE_ID, pageId.toString());
    MDC.put(LoggingConstants.DEVICE_ID, deviceId.toString());
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

    return organizationResource
        .organization(organizationId)
        .thenCompose(
            response -> {
              int status = response.getStatus();
              DataResponse<OrganizationDTO> dataResponse =
                  response.readEntity(new GenericType<>() {});

              if (status != 200) {
                log.error(
                    "[SESSION]: Something went wrong while fetching organization organizationId={} response={}",
                    organizationId,
                    dataResponse);
                throw dataResponse.getError().exception();
              }

              // TODO: increment atomic counter
              // TODO: if free plan and counter > 1000 -- rate limit

              if (!deviceId.equals(page.getDeviceId())) {
                log.debug(
                    "[SESSION]: Unrecognized device -- starting a new session organizationId={}",
                    organizationId);
                return createPageAndNewSession(pageId, deviceId, userAgent, ipAddress, page);
              }

              // recognized device; try to link it with an existing session
              return sessionDatasource
                  .findSessionDeviceLink(page.getOrganizationId(), deviceId)
                  .thenCompose(
                      maybeSessionId -> {
                        if (maybeSessionId.isEmpty()) {
                          log.debug(
                              "[SESSION]: Failed to link to an existing session -- starting new session");
                          return createPageAndNewSession(
                              pageId, deviceId, userAgent, ipAddress, page);
                        }
                        UUID sessionId = maybeSessionId.get();
                        MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());
                        log.info(
                            "[SESSION]: Linking page to an existing session pageId={} sessionId={} organizationId={}",
                            pageId,
                            sessionId,
                            organizationId);

                        return pageDatasource.insertPage(pageId, sessionId, deviceId, page);
                      });
            });
  }

  @Traced
  private CompletionStage<PageIdentity> createPageAndNewSession(
      UUID pageId, UUID deviceId, UserAgentDTO userAgent, String ipAddress, CreatePageDTO page) {
    UUID sessionId = UUID.randomUUID();
    MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());
    log.info("[SESSION]: Creating new session");

    // TODO: move this to a async queue processing
    Location location = locationService.lookupByIp(ipAddress);

    return pageDatasource.createPageAndNewSession(
        pageId, sessionId, deviceId, userAgent, location, page);
  }

  public CompletionStage<Optional<PageDTO>> getPage(
      UUID id, UUID sessionId, String organizationId) {
    MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    MDC.put(LoggingConstants.PAGE_ID, id.toString());
    log.debug("[SESSION]: get page by id");
    return pageDatasource.getPage(id, sessionId, organizationId);
  }
}
