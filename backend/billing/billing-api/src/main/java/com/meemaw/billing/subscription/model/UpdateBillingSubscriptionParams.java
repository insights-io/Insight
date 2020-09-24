package com.meemaw.billing.subscription.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateBillingSubscriptionParams {

  String status;
  long currentPeriodStart;
  long currentPeriodEnd;
}
