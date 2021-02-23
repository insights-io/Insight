package com.rebrowse.billing.subscription.model.dto;

import com.rebrowse.billing.subscription.model.SubscriptionPlan;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateSubscriptionDTO {

  @NotBlank(message = "Required")
  String paymentMethodId;

  @NotNull(message = "Required")
  SubscriptionPlan plan;
}
