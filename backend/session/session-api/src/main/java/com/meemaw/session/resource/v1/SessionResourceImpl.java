package com.meemaw.session.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.service.PageService;
import com.meemaw.session.service.SessionSearchService;
import com.meemaw.session.service.SessionService;
import com.meemaw.session.service.SessionSocketService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.Optional;
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
  @Inject SessionSearchService sessionSearchService;

  @Override
  public CompletionStage<Response> createPage(
      CreatePageDTO body, String userAgent, String xForwardedFor) {
    String clientIpAddress = Optional.ofNullable(xForwardedFor).orElse(request.getRemoteAddr());

    return pageService
        .createPage(body, userAgent, clientIpAddress)
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
  public CompletionStage<Response> getPage(UUID sessionId, UUID pageId) {
    String organizationId = principal.user().getOrganizationId();
    return pageService
        .getPage(pageId, sessionId, organizationId)
        .subscribeAsCompletionStage()
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> getSessions() {
    String organizationId = principal.user().getOrganizationId();
    return sessionService
        .getSessions(
            organizationId, SearchDTO.rhsColon(RequestUtils.map(uriInfo.getQueryParameters())))
        .subscribeAsCompletionStage()
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> searchSessions() {
    String organizationId = principal.user().getOrganizationId();
    return sessionSearchService.search(organizationId).thenApply(DataResponse::ok);
  }
}
