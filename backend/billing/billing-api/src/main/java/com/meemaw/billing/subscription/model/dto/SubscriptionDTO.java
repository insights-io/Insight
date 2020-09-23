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
  SubscriptionPlan plan;
  PriceDTO price;
  OffsetDateTime createdAt;

  private static SubscriptionDTO predefined(String organizationId, SubscriptionPlan plan) {
    return new SubscriptionDTO(null, organizationId, plan, PriceDTO.free(), null);
  }

  public static SubscriptionDTO free(String organizationId) {
    return predefined(organizationId, SubscriptionPlan.FREE);
  }

  public static SubscriptionDTO insight() {
    return predefined(INSIGHT_ORGANIZATION_ID, SubscriptionPlan.ENTERPRISE);
  }
}
