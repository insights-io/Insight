package com.meemaw.auth.organization.resource.v1;

import com.meemaw.auth.organization.service.OrganizationService;
import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OrganizationResourceImpl implements OrganizationResource {

  @Inject InsightPrincipal insightPrincipal;
  @Inject OrganizationService organizationService;

  @Override
  public CompletionStage<Response> members() {
    return organizationService
        .members(insightPrincipal.user().getOrganizationId())
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> organization() {
    return organizationService
        .getOrganization(insightPrincipal.user().getOrganizationId())
        .thenApply(
            maybeOrganization ->
                DataResponse.ok(maybeOrganization.orElseThrow(() -> Boom.notFound().exception())));
  }
}
