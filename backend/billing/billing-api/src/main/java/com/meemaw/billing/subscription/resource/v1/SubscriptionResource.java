package com.meemaw.billing.subscription.resource.v1;

import com.meemaw.auth.sso.AuthScheme;
import com.meemaw.auth.sso.Authenticated;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(SubscriptionResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SubscriptionResource {

  String PATH = "/v1/billing/subscriptions";
  String TAG = "Subscriptions";

  @GET
  @Path("{subscriptionId}")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Retrieve a subscription")
  CompletionStage<Response> retrieve(@PathParam("subscriptionId") String subscriptionId);

  @POST
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Create a subscription")
  CompletionStage<Response> create(
      @NotNull(message = "Required") @Valid CreateSubscriptionDTO body);

  @GET
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "List subscriptions")
  CompletionStage<Response> list();

  @PATCH
  @Path("{subscriptionId}/cancel")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Cancel a subscription")
  CompletionStage<Response> cancel(@PathParam("subscriptionId") String subscriptionId);

  @GET
  @Path("plan")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Get plan associated with active subscription")
  CompletionStage<Response> getActivePlan();
}
