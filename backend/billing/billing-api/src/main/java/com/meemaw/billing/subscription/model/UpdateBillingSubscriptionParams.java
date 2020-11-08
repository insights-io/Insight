package com.meemaw.billing.subscription.model;

import com.stripe.model.Subscription;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Value
@Builder
public class UpdateBillingSubscriptionParams {

  String status;
  long currentPeriodStart;
  long currentPeriodEnd;
  OffsetDateTime canceledAt;

  public static UpdateBillingSubscriptionParams from(Subscription subscription) {
    return UpdateBillingSubscriptionParams.builder()
        .status(subscription.getStatus())
        .currentPeriodStart(subscription.getCurrentPeriodStart())
        .currentPeriodEnd(subscription.getCurrentPeriodEnd())
        .canceledAt(
            subscription.getCanceledAt() == null
                ? null
                : OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(subscription.getCanceledAt()), ZoneOffset.UTC))
        .build();
  }
}
