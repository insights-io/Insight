package com.rebrowse.auth.organization.resource.v1;

import com.rebrowse.auth.organization.datasource.OrganizationTable;
import com.rebrowse.auth.organization.model.AvatarType;
import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.auth.organization.model.dto.AvatarSetupDTO;
import com.rebrowse.auth.organization.service.OrganizationService;
import com.rebrowse.auth.permissions.AccessManager;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.user.datasource.UserTable;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.UserRole;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.query.UpdateDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class OrganizationResourceImpl implements OrganizationResource {

  @Inject AuthPrincipal authPrincipal;
  @Inject OrganizationService organizationService;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> delete() {
    AuthUser user = authPrincipal.user();
    if (!user.getRole().equals(UserRole.ADMIN)) {
      throw Boom.forbidden().exception();
    }

    return organizationService
        .delete(user.getId(), user.getOrganizationId())
        .thenApply(deleted -> deleted ? DataResponse.noContent() : Boom.notFound().response());
  }

  @Override
  public CompletionStage<Response> listMembers() {
    SearchDTO search =
        SearchDTO.withAllowedFields(UserTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    AuthUser user = authPrincipal.user();
    return organizationService
        .members(user.getOrganizationId(), search)
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> memberCount() {
    SearchDTO search =
        SearchDTO.withAllowedFields(UserTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    AuthUser user = authPrincipal.user();

    return organizationService
        .memberCount(user.getOrganizationId(), search)
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> updateAssociated(Map<String, Object> params) {
    AuthUser user = authPrincipal.user();
    if (params.isEmpty()) {
      return CompletableFuture.completedStage(Boom.bodyRequired().response());
    }

    UpdateDTO update = UpdateDTO.from(params);
    Map<String, String> errors = update.validate(OrganizationTable.UPDATABLE_FIELDS);
    if (!errors.isEmpty()) {
      return CompletableFuture.completedStage(Boom.badRequest().errors(errors).response());
    }

    return organizationService
        .updateOrganization(user.getOrganizationId(), update)
        .thenApply(this::mapOrganization);
  }

  @Override
  public CompletionStage<Response> retrieveAssociated() {
    AuthUser user = authPrincipal.user();
    return organizationService
        .getOrganization(user.getOrganizationId())
        .thenApply(this::mapOrganization);
  }

  @Override
  public CompletionStage<Response> associatedAvatarSetup(AvatarSetupDTO body) {
    String image = Optional.ofNullable(body.getImage()).orElse("");
    if (body.getType().equals(AvatarType.AVATAR) && image.isBlank()) {
      return CompletableFuture.completedStage(
          Boom.validationErrors(Map.of("image", "Required")).response());
    }

    String organizationId = authPrincipal.user().getOrganizationId();
    return organizationService.setupAvatar(organizationId, body).thenApply(this::mapOrganization);
  }

  @Override
  public CompletionStage<Response> retrieve(String organizationId) {
    AuthUser user = authPrincipal.user();
    AccessManager.assertCanReadOrganization(user, organizationId);
    return organizationService.getOrganization(organizationId).thenApply(this::mapOrganization);
  }

  private Response mapOrganization(Optional<Organization> organization) {
    return DataResponse.ok(organization.orElseThrow(() -> Boom.notFound().exception()));
  }
}
