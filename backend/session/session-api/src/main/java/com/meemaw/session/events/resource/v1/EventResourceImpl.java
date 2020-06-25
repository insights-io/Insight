package com.meemaw.session.events.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.session.events.service.EventsSearchService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class EventResourceImpl implements EventsResource {

  @Inject EventsSearchService eventsSearchService;
  @Inject InsightPrincipal insightPrincipal;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> search(UUID sessionId) {
    String organizationId = insightPrincipal.user().getOrganizationId();
    Map<String, List<String>> queryParams = RequestUtils.map(uriInfo.getQueryParameters());

    return eventsSearchService
        .search(sessionId, organizationId, SearchDTO.rhsColon(queryParams))
        .thenApply(DataResponse::ok);
  }
}
