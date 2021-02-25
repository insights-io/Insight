package com.rebrowse.session.pages.service;

import com.rebrowse.location.model.Located;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.net.RequestOptions;
import com.rebrowse.session.location.service.LocationService;
import com.rebrowse.session.model.PageVisitCreateParams;
import com.rebrowse.session.model.PageVisitDTO;
import com.rebrowse.session.model.PageVisitSessionLink;
import com.rebrowse.session.pages.datasource.PageVisitDatasource;
import com.rebrowse.session.sessions.datasource.SessionCountDatasource;
import com.rebrowse.session.sessions.datasource.SessionDatasource;
import com.rebrowse.session.useragent.service.UserAgentService;
import com.rebrowse.shared.logging.LoggingConstants;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.useragent.model.UserAgent;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class PageVisitService {

  @Inject UserAgentService userAgentService;
  @Inject LocationService locationService;
  @Inject SessionDatasource sessionDatasource;
  @Inject PageVisitDatasource pageVisitDatasource;
  @Inject SessionCountDatasource sessionCountDatasource;

  @ConfigProperty(name = "authorization.s2s.api.key")
  String s2sApiKey;

  @ConfigProperty(name = "auth-api/mp-rest/url")
  String authApiBaseUrl;

  private RequestOptions authApiRequestOptions() {
    return new RequestOptions.Builder().apiKey(s2sApiKey).apiBaseUrl(authApiBaseUrl).build();
  }

  /**
   * Create a new page visit. This method is called as a first action of the tracking script to link
   * page visits into sessions.
   *
   * <p>1. Check if the organization exists. 2. Increment the session usage counter 3. Check if
   * organization is on free plan and if it has exceeded the free usage 4. If device id is missing,
   * we cannot link it to any existing session thus a new session is created. If device id is
   * included, we try to link it to a existing session active in last 45 minutes. In case such
   * session is not found (doesn't exist or it is more than 45 minutes old), new session is created.
   *
   * @param pageVisit containing all information about the page visit
   * @param userAgentString obtained from User-Agent header
   * @param ipAddress page visit request ip address
   * @return PageIdentity
   */
  @Timed(
      name = "createPageVisit",
      description = "A measure of how long it takes to create a page visit")
  @Traced
  public CompletionStage<PageVisitSessionLink> create(
      PageVisitCreateParams pageVisit, String userAgentString, String ipAddress) {
    UserAgent userAgent = userAgentService.parse(userAgentString);
    String organizationId = pageVisit.getOrganizationId();
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

    if (userAgent.isRobot() || userAgent.isHacker()) {
      log.debug(
          "[SESSION]: Create page visit attempt by deviceClass={} ip={} userAgentString={}",
          ipAddress,
          userAgent.getDeviceClass(),
          userAgentString);
      throw Boom.badRequest().message("You're a robot").exception();
    }

    UUID pageVisitId = UUID.randomUUID();
    UUID deviceId = Optional.ofNullable(pageVisit.getDeviceId()).orElseGet(UUID::randomUUID);
    MDC.put(LoggingConstants.PAGE_VISIT_ID, pageVisitId.toString());
    MDC.put(LoggingConstants.DEVICE_ID, deviceId.toString());

    return Organization.retrieve(organizationId, authApiRequestOptions())
        .thenCompose(
            organization -> {
              if (!deviceId.equals(pageVisit.getDeviceId())) {
                log.debug(
                    "[SESSION]: Unrecognized device -- starting a new session organizationId={}",
                    organizationId);
                return createPageVisit(
                    pageVisitId, deviceId, userAgent, ipAddress, pageVisit, organization);
              }

              // recognized device; try to link it with an existing session
              return sessionDatasource
                  .retrieveByDeviceId(organizationId, deviceId)
                  .thenCompose(
                      maybeSessionId -> {
                        if (maybeSessionId.isEmpty()) {
                          log.debug(
                              "[SESSION]: Could not link page visit to an existing session -- starting new session");
                          return createPageVisit(
                              pageVisitId, deviceId, userAgent, ipAddress, pageVisit, organization);
                        }
                        UUID sessionId = maybeSessionId.get();
                        MDC.put(LoggingConstants.SESSION_ID, sessionId.toString());

                        log.debug(
                            "[SESSION]: Linking page visit to an existing session pageVisitId={} sessionId={} organizationId={}",
                            pageVisitId,
                            sessionId,
                            organizationId);

                        return pageVisitDatasource.create(
                            pageVisitId, sessionId, deviceId, pageVisit);
                      });
            });
  }

  @Traced
  private CompletionStage<PageVisitSessionLink> createPageVisit(
      UUID pageVisitId,
      UUID deviceId,
      UserAgent userAgent,
      String ipAddress,
      PageVisitCreateParams page,
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
                  .createPageAndNewSession(
                      pageVisitId, sessionId, deviceId, userAgent, location, page)
                  .thenApply(
                      identity -> {
                        log.debug(
                            "[SESSION]: Page visit created sessionId={} pageVisitId={} organizationId={}",
                            sessionId,
                            identity.getPageVisitId(),
                            organizationId);

                        return identity;
                      });
            });
  }

  public CompletionStage<Optional<PageVisitDTO>> retrieve(UUID id, String organizationId) {
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    MDC.put(LoggingConstants.PAGE_VISIT_ID, id.toString());
    log.debug("[SESSION]: Retrieve page visit by id={} organizationId={}", id, organizationId);
    return pageVisitDatasource.retrieve(id, organizationId);
  }
}
