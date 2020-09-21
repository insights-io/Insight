package com.meemaw.billing.subscription.model;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class BillingSubscription {

  String id;
  SubscriptionPlan plan;
  String customerExternalId;
  String customerInternalId;
  String priceId;
  long currentPeriodEnd;
  OffsetDateTime createdAt;
  OffsetDateTime canceledAt;
}
