package com.rebrowse.auth.organization.datasource;

import com.rebrowse.auth.organization.model.CreateOrganizationParams;
import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.shared.rest.query.UpdateDTO;
import com.rebrowse.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OrganizationDatasource {

  CompletionStage<Organization> create(CreateOrganizationParams params, SqlTransaction transaction);

  CompletionStage<Organization> create(CreateOrganizationParams params);

  CompletionStage<Optional<Organization>> update(String organizationId, UpdateDTO update);

  CompletionStage<Optional<Organization>> retrieve(String organizationId);

  CompletionStage<Optional<Organization>> retrieve(
      String organizationId, SqlTransaction transaction);

  CompletionStage<Boolean> delete(String id, SqlTransaction transaction);

  CompletionStage<SqlTransaction> transaction();
}
