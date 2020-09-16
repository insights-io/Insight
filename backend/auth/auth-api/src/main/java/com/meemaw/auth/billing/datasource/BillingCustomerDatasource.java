package com.meemaw.auth.billing.datasource;

import com.meemaw.auth.billing.model.BillingCustomer;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingCustomerDatasource {

  CompletionStage<Optional<BillingCustomer>> findByOrganization(String organizationId);

  CompletionStage<BillingCustomer> create(String organizationId, String customerId);
}
