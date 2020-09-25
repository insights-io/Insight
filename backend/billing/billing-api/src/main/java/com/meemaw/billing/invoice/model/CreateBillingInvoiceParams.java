package com.meemaw.billing.invoice.model;

import lombok.Value;

@Value
public class CreateBillingInvoiceParams {

  String id;
  String subscriptionId;
  String paymentIntent;
  String currency;
  long amountPaid;
  long amountDue;
  String status;
}
