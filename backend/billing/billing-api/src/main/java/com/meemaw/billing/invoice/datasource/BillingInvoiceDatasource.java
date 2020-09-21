package com.meemaw.billing.invoice.datasource;

import com.meemaw.billing.invoice.model.BillingInvoice;
import com.meemaw.billing.invoice.model.CreateBillingInvoiceParams;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface BillingInvoiceDatasource {

  CompletionStage<BillingInvoice> create(CreateBillingInvoiceParams params);

  CompletionStage<List<BillingInvoice>> listBySubscription(String subscriptionId);
}
