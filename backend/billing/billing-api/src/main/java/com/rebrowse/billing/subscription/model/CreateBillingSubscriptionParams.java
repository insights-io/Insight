package com.rebrowse.billing.subscription.model;

import lombok.Value;

@Value
public class CreateBillingSubscriptionParams {

  String id;
  SubscriptionPlan plan;
  String customerExternalId;
  String customerInternalId;
  String status;
  String priceId;
  long currentPeriodStart;
  long currentPeriodEnd;
}
