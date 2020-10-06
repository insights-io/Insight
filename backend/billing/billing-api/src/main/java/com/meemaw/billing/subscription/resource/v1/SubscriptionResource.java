package com.meemaw.billing.subscription.resource.v1;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SessionCookieSecurityScheme;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(SubscriptionResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SubscriptionResource {

  String PATH = "/v1/billing/subscriptions";
  String TAG = "Subscriptions";

  @GET
  @Path("{subscriptionId}")
  @Tag(name = TAG)
  @Operation(summary = "Retrieve a subscription")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  CompletionStage<Response> retrieve(@PathParam("subscriptionId") String subscriptionId);

  @POST
  @Tag(name = TAG)
  @Operation(summary = "Create a subscription")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  CompletionStage<Response> create(
      @NotNull(message = "Required") @Valid CreateSubscriptionDTO body);

  @GET
  @Tag(name = TAG)
  @Operation(summary = "List subscriptions")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  CompletionStage<Response> list();

  @PATCH
  @Path("{subscriptionId}/cancel")
  @Tag(name = TAG)
  @Operation(summary = "Cancel a subscription")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  CompletionStage<Response> cancel(@PathParam("subscriptionId") String subscriptionId);

  @GET
  @Path("plan")
  @Tag(name = TAG)
  @Operation(summary = "Get plan associated with active subscription")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  CompletionStage<Response> getActivePlan();
}
