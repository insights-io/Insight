package com.meemaw.session.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.session.datasource.SessionDatasource;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.service.PageService;
import com.meemaw.session.service.SessionSearchService;
import com.meemaw.session.service.SessionSocketService;
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

  @Inject InsightPrincipal principal;
  @Context HttpServletRequest request;
  @Context UriInfo uriInfo;

  @Inject SessionDatasource sessionDatasource;
  @Inject PageService pageService;
  @Inject SessionSocketService sessionSocketService;
  @Inject SessionSearchService sessionSearchService;

  @Override
  public CompletionStage<Response> createPage(
      CreatePageDTO body, String userAgent, String xForwarderFor) {
    String clientIpAddress = Optional.ofNullable(xForwarderFor).orElse(request.getRemoteAddr());

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
  public CompletionStage<Response> count() {
    return pageService.activePageCount().subscribeAsCompletionStage().thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> getSession(UUID sessionId) {
    return sessionDatasource
        .getSession(sessionId, principal.user().getOrganizationId())
        .subscribeAsCompletionStage()
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> getPage(UUID sessionId, UUID pageId) {
    return pageService
        .getPage(pageId, sessionId, principal.user().getOrganizationId())
        .subscribeAsCompletionStage()
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> search() {
    return sessionSearchService.search().thenApply(DataResponse::ok);
  }
}
