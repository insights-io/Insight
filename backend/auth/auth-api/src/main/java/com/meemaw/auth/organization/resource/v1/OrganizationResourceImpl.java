package com.meemaw.auth.organization.resource.v1;

import com.meemaw.auth.organization.datasource.OrganizationTable;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.service.OrganizationService;
import com.meemaw.auth.permissions.AccessManager;
import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.query.UpdateDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OrganizationResourceImpl implements OrganizationResource {

  @Inject InsightPrincipal insightPrincipal;
  @Inject OrganizationService organizationService;

  @Override
  public CompletionStage<Response> retrieveMembers() {
    AuthUser user = insightPrincipal.user();
    return organizationService.members(user.getOrganizationId()).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> updateAssociated(Map<String, Object> params) {
    AuthUser user = insightPrincipal.user();
    if (params.isEmpty()) {
      return CompletableFuture.completedStage(Boom.badRequest().message("Empty update").response());
    }

    UpdateDTO update = UpdateDTO.from(params, OrganizationTable.UPDATABLE_FIELDS);
    Map<String, String> errors = update.validate();
    if (!errors.isEmpty()) {
      return CompletableFuture.completedStage(Boom.badRequest().errors(errors).response());
    }

    return organizationService
        .updateOrganization(user.getOrganizationId(), update)
        .thenApply(
            maybeOrganization -> {
              if (maybeOrganization.isEmpty()) {
                return Boom.notFound().response();
              }
              return DataResponse.ok(maybeOrganization.get());
            });
  }

  @Override
  public CompletionStage<Response> retrieveAssociated() {
    AuthUser user = insightPrincipal.user();
    return organizationService
        .getOrganization(user.getOrganizationId())
        .thenApply(this::mapOrganization);
  }

  @Override
  public CompletionStage<Response> retrieve(String organizationId) {
    AuthUser user = insightPrincipal.user();
    AccessManager.assertCanReadOrganization(user, organizationId);
    return organizationService.getOrganization(organizationId).thenApply(this::mapOrganization);
  }

  private Response mapOrganization(Optional<Organization> organization) {
    return DataResponse.ok(organization.orElseThrow(() -> Boom.notFound().exception()));
  }
}
