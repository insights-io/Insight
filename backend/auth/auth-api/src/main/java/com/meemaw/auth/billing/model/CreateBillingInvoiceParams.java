package com.meemaw.auth.billing.model;

import lombok.Value;

@Value
public class CreateBillingInvoiceParams {

  String id;
  String customerId;
  String subscriptionId;
  String organizationId;
  String currency;
  long amountPaid;
}
