package com.meemaw.auth.billing.model;

import lombok.Value;

@Value
public class CreateBillingSubscriptionParams {

  String id;
  String organizationId;
  String priceId;
  long currentPeriodEnd;
}
