package com.meemaw.billing.invoice.resource.v1;

import com.meemaw.auth.sso.AuthScheme;
import com.meemaw.auth.sso.Authenticated;
import com.meemaw.billing.subscription.resource.v1.SubscriptionResource;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(InvoiceResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface InvoiceResource {

  String PATH = SubscriptionResource.PATH;
  String TAG = "Invoices";

  @GET
  @Path("{subscriptionId}/invoices")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "List invoices associated with subscription")
  CompletionStage<Response> list(@PathParam("subscriptionId") String subscriptionId);

  @GET
  @Path("invoices")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "List invoices")
  CompletionStage<Response> list();
}
