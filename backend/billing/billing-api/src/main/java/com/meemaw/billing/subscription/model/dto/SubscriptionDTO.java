package com.meemaw.billing.subscription.model.dto;

import com.meemaw.billing.subscription.model.SubscriptionPlan;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class SubscriptionDTO {

  String id;
  SubscriptionPlan plan;
  String organizationId;
  String status;
  String priceId;
  long currentPeriodStart;
  long currentPeriodEnd;
  OffsetDateTime createdAt;
  OffsetDateTime canceledAt;
}
