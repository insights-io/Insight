package com.meemaw.auth.organization.datasource;

import com.meemaw.auth.organization.model.Organization;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OrganizationDatasource {

  CompletionStage<Organization> createOrganization(
      String organizationId, String company, Transaction transaction);

  CompletionStage<Optional<Organization>> findOrganization(String organizationId);

  CompletionStage<Optional<Organization>> findOrganization(
      String organizationId, Transaction transaction);
}
