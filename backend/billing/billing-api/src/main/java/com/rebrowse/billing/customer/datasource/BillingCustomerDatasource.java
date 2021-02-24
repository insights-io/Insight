package com.rebrowse.billing.customer.datasource;

import com.rebrowse.billing.customer.model.BillingCustomer;
import com.rebrowse.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingCustomerDatasource {

  CompletionStage<Optional<BillingCustomer>> getByExternalId(String externalId);

  CompletionStage<Optional<BillingCustomer>> getByExternalId(
      String externalId, SqlTransaction transaction);

  CompletionStage<Optional<BillingCustomer>> getByInternalId(String internalId);

  CompletionStage<BillingCustomer> create(String externalId, String internalId);
}
