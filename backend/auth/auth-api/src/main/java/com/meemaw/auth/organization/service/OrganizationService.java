package com.meemaw.auth.organization.service;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.query.UpdateDTO;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OrganizationService {

  CompletionStage<Collection<AuthUser>> members(String organizationId);

  CompletionStage<Optional<Organization>> getOrganization(String organizationId);

  CompletionStage<Optional<Organization>> updateOrganization(
      String organizationId, UpdateDTO update);
}
