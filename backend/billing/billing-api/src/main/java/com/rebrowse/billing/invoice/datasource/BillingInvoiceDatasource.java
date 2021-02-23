package com.rebrowse.billing.invoice.datasource;

import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.billing.invoice.model.BillingInvoice;
import com.rebrowse.billing.invoice.model.CreateBillingInvoiceParams;
import com.rebrowse.billing.invoice.model.UpdateBillingInvoiceParams;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingInvoiceDatasource {

  CompletionStage<Optional<BillingInvoice>> get(String invoiceId);

  CompletionStage<BillingInvoice> create(CreateBillingInvoiceParams params);

  CompletionStage<BillingInvoice> create(
      CreateBillingInvoiceParams params, SqlTransaction transaction);

  CompletionStage<Optional<BillingInvoice>> update(
      String invoiceId, UpdateBillingInvoiceParams params);

  CompletionStage<Optional<BillingInvoice>> update(
      String invoiceId, UpdateBillingInvoiceParams params, SqlTransaction transaction);

  CompletionStage<List<BillingInvoice>> list(String customerInternalId);

  CompletionStage<List<BillingInvoice>> listBySubscription(
      String subscriptionId, String customerInternalId);

  CompletionStage<SqlTransaction> startTransaction();
}
