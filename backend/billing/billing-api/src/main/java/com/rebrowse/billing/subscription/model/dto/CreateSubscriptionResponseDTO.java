package com.rebrowse.billing.subscription.model.dto;

import com.stripe.model.PaymentIntent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateSubscriptionResponseDTO {

  PlanDTO plan;
  String clientSecret;

  public static CreateSubscriptionResponseDTO create(PlanDTO plan, PaymentIntent paymentIntent) {
    if ("succeeded".equals(paymentIntent.getStatus())) {
      return new CreateSubscriptionResponseDTO(plan, null);
    }
    return new CreateSubscriptionResponseDTO(null, paymentIntent.getClientSecret());
  }
}
