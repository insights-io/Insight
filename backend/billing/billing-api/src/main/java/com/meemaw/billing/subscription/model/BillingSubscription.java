package com.meemaw.billing.subscription.model;

import lombok.Value;

import com.meemaw.billing.subscription.model.dto.SubscriptionDTO;

import java.time.OffsetDateTime;

@Value
public class BillingSubscription {

  String id;
  SubscriptionPlan plan;
  String customerExternalId;
  String customerInternalId;
  String status;
  String priceId;
  long currentPeriodStart;
  long currentPeriodEnd;
  OffsetDateTime createdAt;
  OffsetDateTime canceledAt;

  public SubscriptionDTO dto() {
    return new SubscriptionDTO(
        id,
        plan,
        customerInternalId,
        status,
        priceId,
        currentPeriodStart,
        currentPeriodEnd,
        createdAt,
        canceledAt);
  }
}
