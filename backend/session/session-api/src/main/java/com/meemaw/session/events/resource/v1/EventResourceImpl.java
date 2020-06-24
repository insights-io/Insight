package com.meemaw.session.events.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.session.events.service.EventsSearchService;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class EventResourceImpl implements EventsResource {

  @Inject EventsSearchService eventsSearchService;
  @Inject InsightPrincipal insightPrincipal;

  @Override
  public CompletionStage<Response> search(UUID sessionId) {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return eventsSearchService.search(sessionId, organizationId).thenApply(DataResponse::ok);
  }
}
