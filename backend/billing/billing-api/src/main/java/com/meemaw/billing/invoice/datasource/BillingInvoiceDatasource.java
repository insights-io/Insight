package com.meemaw.billing.invoice.datasource;

import com.meemaw.billing.invoice.model.BillingInvoice;
import com.meemaw.billing.invoice.model.CreateBillingInvoiceParams;
import com.meemaw.billing.invoice.model.UpdateBillingInvoiceParams;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingInvoiceDatasource {

  CompletionStage<Optional<BillingInvoice>> get(String invoiceId);

  CompletionStage<BillingInvoice> create(CreateBillingInvoiceParams params);

  CompletionStage<Optional<BillingInvoice>> update(
      String invoiceId, UpdateBillingInvoiceParams params);

  CompletionStage<List<BillingInvoice>> listBySubscription(
      String subscriptionId, String customerInternalId);
}
