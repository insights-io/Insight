package com.meemaw.billing.invoice.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateBillingInvoiceParams {

  long amountPaid;
  long amountDue;
  String status;
}
