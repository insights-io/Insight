package com.meemaw.session.sessions.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.pages.service.PageService;
import com.meemaw.session.resource.v1.SessionResource;
import com.meemaw.session.sessions.service.SessionService;
import com.meemaw.session.sessions.service.SessionSocketService;
import com.meemaw.session.useragent.service.UserAgentService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionResourceImpl implements SessionResource {

  @Context HttpServletRequest request;
  @Context UriInfo uriInfo;
  @Inject InsightPrincipal principal;
  @Inject SessionService sessionService;
  @Inject PageService pageService;
  @Inject SessionSocketService sessionSocketService;
  @Inject UserAgentService userAgentService;

  @Override
  public CompletionStage<Response> createPage(CreatePageDTO body, String userAgentString) {
    String ipAddress = RequestUtils.getRemoteAddress(request);

    return pageService
        .createPage(body, userAgentString, ipAddress)
        .subscribeAsCompletionStage()
        .thenApply(
            pageIdentity -> {
              sessionSocketService.pageStart(pageIdentity.getPageId());
              return DataResponse.ok(pageIdentity);
            });
  }

  @Override
  public CompletionStage<Response> getSession(UUID sessionId) {
    String organizationId = principal.user().getOrganizationId();
    return sessionService
        .getSession(sessionId, organizationId)
        .subscribeAsCompletionStage()
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> getPage(UUID sessionId, UUID pageId, String organizationId) {
    // String organizationId = principal.user().getOrganizationId();
    return pageService
        .getPage(pageId, sessionId, organizationId)
        .subscribeAsCompletionStage()
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> getSessions() {
    String organizationId = principal.user().getOrganizationId();
    Map<String, List<String>> queryParams = RequestUtils.map(uriInfo.getQueryParameters());
    return sessionService
        .getSessions(organizationId, SearchDTO.rhsColon(queryParams))
        .subscribeAsCompletionStage()
        .thenApply(DataResponse::ok);
  }
}
