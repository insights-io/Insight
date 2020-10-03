package com.meemaw.session.sessions.resource.v1;

import static com.meemaw.auth.user.UserRegistry.S2S_INTERNAL_USER;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.pages.service.PageService;
import com.meemaw.session.sessions.datasource.SessionTable;
import com.meemaw.session.sessions.service.SessionService;
import com.meemaw.session.sessions.service.SessionSocketService;
import com.meemaw.session.sessions.v1.SessionResource;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
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

  @Override
  public CompletionStage<Response> createPage(CreatePageDTO body, String userAgent) {
    String ipAddress = RequestUtils.getRemoteAddress(request);

    return pageService
        .createPage(body, userAgent, ipAddress)
        .thenApply(
            pageIdentity -> {
              // TODO: should be done in service not resource
              // TODO: notify with session object
              sessionSocketService.pageStart(pageIdentity.getPageId());
              return DataResponse.ok(pageIdentity);
            });
  }

  @Override
  public CompletionStage<Response> getSession(UUID sessionId) {
    String organizationId = principal.user().getOrganizationId();
    return sessionService
        .getSession(sessionId, organizationId)
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> getPage(
      UUID sessionId, UUID pageId, String organizationId, String authorization) {
    // TODO: write a clean module to handle permissions
    AuthUser user = principal.user();
    if (!user.getOrganizationId().equals(S2S_INTERNAL_USER.getOrganizationId())
        && !user.getOrganizationId().equals(organizationId)) {
      throw Boom.notFound().exception();
    }

    return pageService
        .getPage(pageId, sessionId, organizationId)
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> getSessions() {
    String organizationId = principal.user().getOrganizationId();
    SearchDTO searchDTO =
        SearchDTO.withAllowedFields(SessionTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    return sessionService.getSessions(organizationId, searchDTO).thenApply(DataResponse::ok);
  }
}
