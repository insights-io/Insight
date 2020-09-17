package com.meemaw.auth.billing.datasource;

import com.meemaw.auth.billing.model.BillingSubscription;
import com.meemaw.auth.billing.model.CreateBillingSubscriptionParams;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingSubscriptionDatasource {

  CompletionStage<BillingSubscription> create(CreateBillingSubscriptionParams params);

  CompletionStage<Optional<BillingSubscription>> findByOrganizationId(String organizationId);
}
