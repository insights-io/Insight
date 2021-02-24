package com.rebrowse.billing.invoice.model;

import com.stripe.model.Invoice;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateBillingInvoiceParams {

  long amountPaid;
  long amountDue;
  String status;

  public static UpdateBillingInvoiceParams from(Invoice invoice) {
    return UpdateBillingInvoiceParams.builder()
        .status(invoice.getStatus())
        .amountPaid(invoice.getAmountPaid())
        .amountDue(invoice.getAmountDue())
        .build();
  }
}
