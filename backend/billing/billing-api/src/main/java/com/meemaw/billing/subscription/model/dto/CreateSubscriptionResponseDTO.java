package com.meemaw.billing.subscription.model.dto;

import com.stripe.model.PaymentIntent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateSubscriptionResponseDTO {

  SubscriptionDTO subscription;
  String clientSecret;

  public static CreateSubscriptionResponseDTO create(
      SubscriptionDTO subscription, PaymentIntent paymentIntent) {
    if ("succeeded".equals(paymentIntent.getStatus())) {
      return new CreateSubscriptionResponseDTO(subscription, null);
    }
    return new CreateSubscriptionResponseDTO(null, paymentIntent.getClientSecret());
  }
}
