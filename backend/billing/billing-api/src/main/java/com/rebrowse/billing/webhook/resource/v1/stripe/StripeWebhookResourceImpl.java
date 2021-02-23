package com.rebrowse.billing.webhook.resource.v1.stripe;

import com.rebrowse.billing.webhook.service.WebhookProcessor;
import com.stripe.model.Event;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class StripeWebhookResourceImpl implements StripeWebhookResource {

  @Inject WebhookProcessor<Event> webhookProcessor;

  @Override
  public CompletionStage<Response> webhook(String body, String stripeSignature) {
    return webhookProcessor
        .process(body, stripeSignature)
        .thenApply(ignored -> Response.noContent().build());
  }
}
