package com.meemaw.billing.invoice.model;

import lombok.Value;

@Value
public class CreateBillingInvoiceParams {

  String id;
  String subscriptionId;
  String currency;
  long amountPaid;
}
