package com.meemaw.session.sessions.resource.v1;

import com.meemaw.auth.permissions.AccessManager;
import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.session.model.CreatePageDTO;
import com.meemaw.session.pages.service.PageService;
import com.meemaw.session.sessions.service.SessionSocketService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class SessionPageResourceImpl implements SessionPageResource {

  @Context HttpServletRequest request;
  @Context UriInfo uriInfo;
  @Inject InsightPrincipal principal;
  @Inject PageService pageService;
  @Inject SessionSocketService sessionSocketService;

  @Override
  public CompletionStage<Response> create(CreatePageDTO body, String userAgent) {
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
  public CompletionStage<Response> retrieve(
      UUID sessionId, UUID pageId, @Nullable String organizationId) {
    AuthUser user = principal.user();
    String actualOrganizationId =
        Optional.ofNullable(organizationId).orElseGet(user::getOrganizationId);

    AccessManager.assertCanReadOrganization(user, actualOrganizationId);
    return pageService
        .getPage(pageId, sessionId, actualOrganizationId)
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }
}
