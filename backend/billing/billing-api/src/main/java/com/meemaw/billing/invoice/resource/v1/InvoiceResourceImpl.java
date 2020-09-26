package com.meemaw.billing.invoice.resource.v1;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.billing.invoice.service.InvoiceService;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class InvoiceResourceImpl implements InvoiceResource {

  @Inject InsightPrincipal principal;
  @Inject InvoiceService invoiceService;

  @Override
  public CompletionStage<Response> listInvoices(String subscriptionId) {
    String organizationId = principal.user().getOrganizationId();
    return invoiceService.listInvoices(subscriptionId, organizationId).thenApply(DataResponse::ok);
  }
}
