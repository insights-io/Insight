package com.rebrowse.billing.subscription.model.dto;

import static com.rebrowse.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;

import com.rebrowse.billing.subscription.model.SubscriptionPlan;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PlanDTO {

  String subscriptionId;
  String organizationId;
  SubscriptionPlan type;
  String dataRetention;
  PriceDTO price;
  OffsetDateTime createdAt;

  private static PlanDTO free(String dataRetention, String organizationId, SubscriptionPlan plan) {
    return new PlanDTO(null, organizationId, plan, dataRetention, PriceDTO.free(), null);
  }

  public static PlanDTO free(String organizationId) {
    return free("1m", organizationId, SubscriptionPlan.FREE);
  }

  public static PlanDTO genesis() {
    return free("∞", REBROWSE_ORGANIZATION_ID, SubscriptionPlan.ENTERPRISE);
  }
}
