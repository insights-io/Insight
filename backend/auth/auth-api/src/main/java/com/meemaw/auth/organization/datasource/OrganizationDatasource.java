package com.meemaw.auth.organization.datasource;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OrganizationDatasource {

  CompletionStage<Organization> createOrganization(
      String organizationId, String company, SqlTransaction transaction);

  CompletionStage<Optional<Organization>> findOrganization(String organizationId);

  CompletionStage<Optional<Organization>> findOrganization(
      String organizationId, SqlTransaction transaction);
}
