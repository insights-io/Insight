package com.meemaw.billing.subscription.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(SubscriptionResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SubscriptionResource {

  String PATH = "/v1/billing/subscriptions";

  @POST
  @CookieAuth
  CompletionStage<Response> create(
      @NotNull(message = "Required") @Valid CreateSubscriptionDTO body);

  @GET
  @CookieAuth
  CompletionStage<Response> get();

  @DELETE
  @CookieAuth
  CompletionStage<Response> cancel();
}
