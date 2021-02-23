package com.rebrowse.billing.utils;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.PaymentMethodCreateParams.CardDetails;
import com.stripe.param.PaymentMethodCreateParams.Type;

public final class BillingTestUtils {

  private BillingTestUtils() {}

  public static PaymentMethod createVisaTestPaymentMethod() throws StripeException {
    return PaymentMethod.create(
        PaymentMethodCreateParams.builder()
            .setType(Type.CARD)
            .setCard(
                CardDetails.builder()
                    .setNumber("4242 4242 4242 4242")
                    .setCvc("222")
                    .setExpMonth(10L)
                    .setExpYear(22L)
                    .build())
            .build());
  }

  public static PaymentMethod create3DSecurePaymentMethod() throws StripeException {
    return PaymentMethod.create(
        PaymentMethodCreateParams.builder()
            .setType(Type.CARD)
            .setCard(
                CardDetails.builder()
                    .setNumber("4000 0000 0000 3220")
                    .setCvc("222")
                    .setExpMonth(10L)
                    .setExpYear(22L)
                    .build())
            .build());
  }
}
