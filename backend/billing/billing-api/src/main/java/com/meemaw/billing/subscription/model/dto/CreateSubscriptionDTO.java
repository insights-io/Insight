package com.meemaw.billing.subscription.model.dto;

import javax.validation.constraints.NotBlank;
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
}
