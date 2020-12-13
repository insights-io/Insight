package com.meemaw.session.insights.resource.v1;

import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.session.sessions.datasource.SessionTable;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class InsightsResourceImpl implements InsightsResource {

  @Inject AuthPrincipal authPrincipal;
  @Inject SessionDatasource sessionDatasource;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> distinct(List<String> on) {
    Map<String, String> errors = new HashMap<>();
    for (String field : on) {
      if (!SessionTable.QUERYABLE_FIELDS.contains(field)) {
        errors.put(field, "Unexpected field");
      }
    }

    if (!errors.isEmpty()) {
      return CompletableFuture.completedStage(Boom.badRequest().errors(errors).response());
    }

    String organizationId = authPrincipal.user().getOrganizationId();
    Map<String, List<String>> params = RequestUtils.map(uriInfo.getQueryParameters());
    params.remove(ON);
    SearchDTO search = SearchDTO.withAllowedFields(SessionTable.QUERYABLE_FIELDS).rhsColon(params);
    return sessionDatasource.distinct(on, organizationId, search).thenApply(DataResponse::ok);
  }
}
