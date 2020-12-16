package com.meemaw.session.events.resource.v1;

import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.session.events.datasource.EventTable;
import com.meemaw.session.events.service.EventsSearchService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.DataResponse;
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
