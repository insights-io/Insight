package com.meemaw.session.pages.service;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.organization.resource.v1.OrganizationResource;
import com.meemaw.auth.sso.bearer.AbstractBearerTokenSecurityRequirementAuthDynamicFeature;
import com.meemaw.location.model.Located;
import com.meemaw.session.location.service.LocationService;
import com.meemaw.session.model.CreatePageVisitDTO;
import com.meemaw.session.model.PageVisitDTO;
import com.meemaw.session.model.PageVisitSessionLink;
import com.meemaw.session.pages.datasource.PageVisitDatasource;
import com.meemaw.session.sessions.datasource.SessionCountDatasource;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.session.useragent.service.UserAgentService;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.useragent.model.UserAgent;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class PageVisitService {

  @Inject UserAgentService userAgentService;
  @Inject LocationService locationService;
  @Inject SessionDatasource sessionDatasource;
  @Inject PageVisitDatasource pageVisitDatasource;
  @Inject SessionCountDatasource sessionCountDatasource;
  @Inject @RestClient OrganizationResource organizationResource;

  @ConfigProperty(name = "authorization.s2s.api.key")
  String s2sApiKey;

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
   * @param page containing all information about the page visit
   * @param userAgentString obtained from User-Agent header
   * @param ipAddress page visit request ip address
   * @return PageIdentity
   */
  @Timed(name = "createPage", description = "A measure of how long it takes to create a page")
  @Traced
  public CompletionStage<PageVisitSessionLink> create(
      CreatePageVisitDTO page, String userAgentString, String ipAddress) {
    UserAgent userAgent = userAgentService.parse(userAgentString);
    if (userAgent.isRobot() || userAgent.isHacker()) {
      log.debug(
          "[SESSION]: Create page attempt by robot ip={} userAgent={}", ipAddress, userAgentString);
      throw Boom.badRequest().message("You're a robot").exception();
    }

    String organizationId = page.getOrganizationId();
    UUID pageId = UUID.randomUUID();
    UUID deviceId = Optional.ofNullable(page.getDeviceId()).orElseGet(UUID::randomUUID);
    MDC.put(LoggingConstants.PAGE_VISIT_ID, pageId.toString());
    MDC.put(LoggingConstants.DEVICE_ID, deviceId.toString());
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

    // TODO: use SDK
    return organizationResource
        .retrieve(
            organizationId,
            AbstractBearerTokenSecurityRequirementAuthDynamicFeature.header(s2sApiKey))
        .thenCompose(
            response -> {
              int status = response.getStatus();
              DataResponse<OrganizationDTO> dataResponse =
                  response.readEntity(new GenericType<>() {});

              if (status != Response.Status.OK.getStatusCode()) {
                log.error(
                    "[SESSION]: Something went wrong while fetching organization organizationId={} response={}",
                    organizationId,
                    dataResponse);
                throw dataResponse.getError().exception();
              }

              Organization organization = dataResponse.getData();
              if (!deviceId.equals(page.getDeviceId())) {
                log.debug(
                    "[SESSION]: Unrecognized device -- starting a new session organizationId={}",
                    organizationId);
                return createPageSession(
                    pageId, deviceId, userAgent, ipAddress, page, organization);
              }

              // recognized device; try to link it with an existing session
              return sessionDatasource
                  .findSessionDeviceLink(page.getOrganizationId(), deviceId)
                  .thenCompose(
                      maybeSessionId -> {
                        if (maybeSessionId.isEmpty()) {
                          log.debug(
                              "[SESSION]: Could not link to an existing session -- starting new session");
                          return createPageSession(
                              pageId, deviceId, userAgent, ipAddress, page, organization);
                        }
                        UUID sessionId = maybeSessionId.get();
                        MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());
                        log.info(
                            "[SESSION]: Linking page to an existing session pageId={} sessionId={} organizationId={}",
                            pageId,
                            sessionId,
                            organizationId);

                        return pageVisitDatasource.create(pageId, sessionId, deviceId, page);
                      });
            });
  }

  @Traced
  private CompletionStage<PageVisitSessionLink> createPageSession(
      UUID pageId,
      UUID deviceId,
      UserAgent userAgent,
      String ipAddress,
      CreatePageVisitDTO page,
      Organization organization) {
    UUID sessionId = UUID.randomUUID();
    MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());

    String organizationId = organization.getId();
    String sessionCounterKey =
        String.format(
            "%s-%s", organizationId, organization.getStartOfCurrentBillingPeriod().toString());

    return sessionCountDatasource
        .incrementAndGet(sessionCounterKey)
        .thenCompose(
            sessionCount -> {
              // TODO: check for plan
              if (sessionCount > 1000) {
                log.debug(
                    "[SESSION]: Create session free quota exceeded organizationId={}",
                    organizationId);

                throw Boom.badRequest()
                    .message(
                        "Free plan quota reached. Please upgrade your plan to continue collecting Insights.")
                    .exception();
              }

              // TODO: move this to a async queue processing
              Located location = locationService.lookupByIp(ipAddress);

              return pageVisitDatasource
                  .createPageAndNewSession(pageId, sessionId, deviceId, userAgent, location, page)
                  .thenApply(
                      identity -> {
                        log.debug(
                            "[SESSION]: Created session id={} organizationId={}",
                            sessionId,
                            organizationId);

                        return identity;
                      });
            });
  }

  public CompletionStage<Optional<PageVisitDTO>> retrieve(UUID id, String organizationId) {
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    MDC.put(LoggingConstants.PAGE_VISIT_ID, id.toString());
    log.debug("[SESSION]: get page by id={} organizationId={}", id, organizationId);
    return pageVisitDatasource.retrieve(id, organizationId);
  }
}
