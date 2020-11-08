package com.meemaw.billing.invoice.service;

import com.meemaw.billing.invoice.datasource.BillingInvoiceDatasource;
import com.meemaw.billing.invoice.model.BillingInvoice;
import com.meemaw.billing.invoice.model.dto.InvoiceDTO;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InvoiceService {

  @Inject BillingInvoiceDatasource invoiceDatasource;

  public CompletionStage<List<InvoiceDTO>> listInvoices(
      String subscriptionId, String organizationId) {
    return invoiceDatasource
        .listBySubscription(subscriptionId, organizationId)
        .thenApply(
            invoices -> invoices.stream().map(BillingInvoice::dto).collect(Collectors.toList()));
  }

  public CompletionStage<List<InvoiceDTO>> listInvoices(String organizationId) {
    return invoiceDatasource
        .list(organizationId)
        .thenApply(
            invoices -> invoices.stream().map(BillingInvoice::dto).collect(Collectors.toList()));
  }
}
