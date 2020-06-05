package com.meemaw.auth.organization.datasource;

import com.meemaw.auth.organization.model.Organization;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OrganizationDatasource {

  /**
   * Create a new organization.
   *
   * @param organizationId String organization id
   * @param company String organization name
   * @param transaction Transaction context
   * @return newly created Organization
   */
  CompletionStage<Organization> createOrganization(
      String organizationId, String company, Transaction transaction);

  /**
   * Find an existing organization.
   *
   * @param organizationId String organization id
   * @return Optional Organization if exists
   */
  CompletionStage<Optional<Organization>> findOrganization(String organizationId);

  /**
   * Find an existing organization.
   *
   * @param organizationId String organization id
   * @param transaction Transaction context
   * @return Optional Organization if exists
   */
  CompletionStage<Optional<Organization>> findOrganization(
      String organizationId, Transaction transaction);
}
