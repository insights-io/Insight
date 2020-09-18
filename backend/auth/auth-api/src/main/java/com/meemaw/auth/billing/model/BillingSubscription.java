package com.meemaw.auth.billing.model;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class BillingSubscription {

  String id;
  String organizationId;
  String priceId;
  long currentPeriodEnd;
  OffsetDateTime createdAt;
}
