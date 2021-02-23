package com.rebrowse.session.pages.resource;

import com.rebrowse.auth.permissions.AccessManager;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.session.model.PageVisitCreateParams;
import com.rebrowse.session.pages.datasource.PageVisitDatasource;
import com.rebrowse.session.pages.datasource.PageVisitTable;
import com.rebrowse.session.pages.resource.v1.PageVisitResource;
import com.rebrowse.session.pages.service.PageVisitService;
import com.rebrowse.session.sessions.service.SessionSocketService;
import com.rebrowse.shared.context.RequestContextUtils;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class PageVisitResourceImpl implements PageVisitResource {

  @Inject AuthPrincipal principal;
  @Inject PageVisitService pageVisitService;
  @Inject PageVisitDatasource pageVisitDatasource;
  @Inject SessionSocketService sessionSocketService;

  @Context HttpServletRequest request;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> create(PageVisitCreateParams body, String userAgent) {
    String ipAddress = RequestContextUtils.getRemoteAddress(request);
    return pageVisitService
        .create(body, userAgent, ipAddress)
        .thenApply(
            pageVisitSessionLink -> {
              // TODO: should be done in service not resource
              // TODO: notify with session object
              sessionSocketService.pageStart(pageVisitSessionLink.getPageVisitId());
              return DataResponse.ok(pageVisitSessionLink);
            });
  }

  @Override
  public CompletionStage<Response> retrieve(UUID pageId, @Nullable String organizationId) {
    AuthUser user = principal.user();
    String actualOrganizationId =
        Optional.ofNullable(organizationId).orElseGet(user::getOrganizationId);

    AccessManager.assertCanReadOrganization(user, actualOrganizationId);
    return pageVisitService
        .retrieve(pageId, actualOrganizationId)
        .thenApply(
            maybePage -> DataResponse.ok(maybePage.orElseThrow(() -> Boom.notFound().exception())));
  }

  @Override
  public CompletionStage<Response> count() {
    String organizationId = principal.user().getOrganizationId();
    SearchDTO search =
        SearchDTO.withAllowedFields(PageVisitTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    return pageVisitDatasource.count(organizationId, search).thenApply(DataResponse::ok);
  }
}
