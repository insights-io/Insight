package com.meemaw.auth.organization.resource.v1;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.service.OrganizationService;
import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.Optional;
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
        .thenApply(this::mapOrganization);
  }

  @Override
  public CompletionStage<Response> organization(String organizationId) {
    return organizationService.getOrganization(organizationId).thenApply(this::mapOrganization);
  }

  private Response mapOrganization(Optional<Organization> organization) {
    return DataResponse.ok(organization.orElseThrow(() -> Boom.notFound().exception()));
  }
}
