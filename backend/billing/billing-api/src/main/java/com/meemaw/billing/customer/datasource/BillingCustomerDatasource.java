package com.meemaw.billing.customer.datasource;

import com.meemaw.billing.customer.model.BillingCustomer;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingCustomerDatasource {

  CompletionStage<Optional<BillingCustomer>> getByExternalId(String externalId);

  CompletionStage<Optional<BillingCustomer>> getByInternalId(String internalId);

  CompletionStage<BillingCustomer> create(String externalId, String internalId);
}
