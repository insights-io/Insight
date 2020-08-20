package com.meemaw.session.insights.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.session.sessions.datasource.SessionTable;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class InsightsResourceImpl implements InsightsResource {

  @Inject InsightPrincipal insightPrincipal;
  @Inject SessionDatasource sessionDatasource;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> count() {
    String organizationId = insightPrincipal.user().getOrganizationId();
    SearchDTO searchDTO =
        SearchDTO.withAllowedFields(SessionTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    return sessionDatasource.count(organizationId, searchDTO).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> distinct(List<String> on) {
    String organizationId = insightPrincipal.user().getOrganizationId();
    Map<String, List<String>> params = RequestUtils.map(uriInfo.getQueryParameters());
    params.remove(ON);
    SearchDTO searchDTO =
        SearchDTO.withAllowedFields(SessionTable.QUERYABLE_FIELDS).rhsColon(params);

    return sessionDatasource.distinct(on, organizationId, searchDTO).thenApply(DataResponse::ok);
  }
}
