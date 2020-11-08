package com.meemaw.billing.webhook.service.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StripeWebhookTransformer {

  @ConfigProperty(name = "billing.stripe.webhook_secret")
  String stripeWebhookSecret;

  public Event construct(String payload, String signature) throws SignatureVerificationException {
    return Webhook.constructEvent(payload, signature, stripeWebhookSecret);
  }
}
