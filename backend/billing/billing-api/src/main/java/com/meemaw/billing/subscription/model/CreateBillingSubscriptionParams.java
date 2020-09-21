package com.meemaw.billing.subscription.model;

import lombok.Value;

@Value
public class CreateBillingSubscriptionParams {

  String id;
  SubscriptionPlan plan;
  String customerExternalId;
  String customerInternalId;
  String priceId;
  long currentPeriodEnd;
}
