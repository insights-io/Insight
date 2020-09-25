package com.meemaw.billing.subscription.model.dto;

import static com.meemaw.shared.SharedConstants.INSIGHT_ORGANIZATION_ID;

import com.meemaw.billing.subscription.model.SubscriptionPlan;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SubscriptionDTO {

  String id;
  String organizationId;
  String status;
  SubscriptionPlan plan;
  PriceDTO price;
  OffsetDateTime createdAt;

  private static SubscriptionDTO active(String organizationId, SubscriptionPlan plan) {
    return new SubscriptionDTO(null, organizationId, "active", plan, PriceDTO.free(), null);
  }

  public static SubscriptionDTO free(String organizationId) {
    return active(organizationId, SubscriptionPlan.FREE);
  }

  public static SubscriptionDTO insight() {
    return active(INSIGHT_ORGANIZATION_ID, SubscriptionPlan.ENTERPRISE);
  }
}
