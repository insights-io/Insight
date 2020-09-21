package com.meemaw.billing.subscription.datasource;

import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.CreateBillingSubscriptionParams;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingSubscriptionDatasource {

  CompletionStage<BillingSubscription> create(CreateBillingSubscriptionParams params);

  CompletionStage<Optional<BillingSubscription>> findByCustomerInternalId(
      String customerInternalId);
}
