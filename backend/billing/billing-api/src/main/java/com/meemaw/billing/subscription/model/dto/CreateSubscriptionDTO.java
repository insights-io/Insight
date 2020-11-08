package com.meemaw.billing.subscription.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.billing.subscription.model.SubscriptionPlan;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateSubscriptionDTO {

  @NotBlank(message = "Required")
  String paymentMethodId;

  @NotNull(message = "Required")
  SubscriptionPlan plan;
}
