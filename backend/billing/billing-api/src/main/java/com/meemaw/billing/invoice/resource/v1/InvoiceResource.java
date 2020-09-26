package com.meemaw.billing.invoice.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.billing.subscription.resource.v1.SubscriptionResource;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(InvoiceResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface InvoiceResource {

  String PATH = SubscriptionResource.PATH;

  @GET
  @Path("{subscriptionId}/invoices")
  @CookieAuth
  CompletionStage<Response> listInvoices(@PathParam("subscriptionId") String subscriptionId);
}
