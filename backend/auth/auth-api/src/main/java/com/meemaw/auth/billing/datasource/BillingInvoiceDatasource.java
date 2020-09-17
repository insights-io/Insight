package com.meemaw.auth.billing.datasource;

import com.meemaw.auth.billing.model.BillingInvoice;
import com.meemaw.auth.billing.model.CreateBillingInvoiceParams;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface BillingInvoiceDatasource {

  CompletionStage<BillingInvoice> create(CreateBillingInvoiceParams params);

  CompletionStage<List<BillingInvoice>> listByOrganizationId(String organizationId);
}
