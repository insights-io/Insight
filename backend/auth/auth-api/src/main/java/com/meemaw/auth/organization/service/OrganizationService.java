package com.meemaw.auth.organization.service;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OrganizationService {
  /**
   * Find organization members.
   *
   * @param organizationId organization id
   * @return user collection
   */
  CompletionStage<Collection<AuthUser>> members(String organizationId);

  /**
   * Get organization.
   *
   * @param organizationId organization id
   * @return maybe organization
   */
  CompletionStage<Optional<Organization>> getOrganization(String organizationId);
}
