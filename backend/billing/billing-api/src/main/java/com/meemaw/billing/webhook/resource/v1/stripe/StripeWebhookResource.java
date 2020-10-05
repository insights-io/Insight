package com.meemaw.billing.webhook.resource.v1.stripe;

import com.meemaw.billing.subscription.resource.v1.SubscriptionResource;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(StripeWebhookResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
public interface StripeWebhookResource {

  String PATH = SubscriptionResource.PATH + "/event";
  String TAG = "Events";

  @POST
  @Tag(name = TAG)
  @Operation(summary = "Webhook event")
  CompletionStage<Response> webhook(
      @NotBlank(message = "Required") String body,
      @NotBlank(message = "Required") @HeaderParam("Stripe-Signature") String stripeSignature);
}
