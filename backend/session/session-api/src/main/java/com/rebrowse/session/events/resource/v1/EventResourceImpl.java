package com.rebrowse.session.events.resource.v1;

import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.session.events.datasource.EventTable;
import com.rebrowse.session.events.service.EventsSearchService;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.response.DataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class EventResourceImpl implements EventsResource {

  @Inject EventsSearchService eventsSearchService;
  @Inject AuthPrincipal authPrincipal;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> search(UUID sessionId) {
    String organizationId = authPrincipal.user().getOrganizationId();
    SearchDTO searchDTO =
        SearchDTO.withAllowedFields(EventTable.QUERYABLE_FIELD)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    return eventsSearchService
        .search(sessionId, organizationId, searchDTO)
        .thenApply(DataResponse::ok);
  }
}
