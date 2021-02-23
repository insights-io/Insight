package com.rebrowse.billing.webhook.service.stripe;

public final class StripeWebhookEvents {

  public static final String INVOICE_FINALIZED = "invoice.finalized";
  public static final String INVOICE_PAID = "invoice.paid";
  public static final String SUBSCRIPTION_UPDATED = "customer.subscription.updated";
  public static final String PAYMENT_INTENT_PAYMENT_FAILED = "payment_intent.payment_failed";

  private StripeWebhookEvents() {}
}
