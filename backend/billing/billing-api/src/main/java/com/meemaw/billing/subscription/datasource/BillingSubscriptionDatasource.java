package com.meemaw.billing.subscription.datasource;

import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.CreateBillingSubscriptionParams;
import com.meemaw.billing.subscription.model.UpdateBillingSubscriptionParams;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingSubscriptionDatasource {

  CompletionStage<BillingSubscription> create(CreateBillingSubscriptionParams params);

  CompletionStage<Optional<BillingSubscription>> update(
      String subscriptionId, UpdateBillingSubscriptionParams params);

  CompletionStage<Boolean> delete(String subscriptionId);

  CompletionStage<Optional<BillingSubscription>> get(String subscriptionId);

  CompletionStage<Optional<BillingSubscription>> get(String subscriptionId, String organizationId);

  CompletionStage<Optional<BillingSubscription>> get(
      String subscriptionId, SqlTransaction transaction);

  CompletionStage<List<BillingSubscription>> listSubscriptionsByCustomerInternalId(
      String customerInternalId);

  CompletionStage<Optional<BillingSubscription>> getByCustomerInternalId(
      String subscriptionId, String customerInternalId);

  CompletionStage<Optional<BillingSubscription>> getByCustomerInternalId(String customerInternalId);

  CompletionStage<Optional<BillingSubscription>> getActiveSubscriptionByCustomerInternalId(
      String customerInternalId);
}
