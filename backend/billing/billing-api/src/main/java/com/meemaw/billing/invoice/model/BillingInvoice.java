package com.meemaw.billing.invoice.model;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class BillingInvoice {

  String id;
  String subscriptionId;
  String currency;
  long amountPaid;
  OffsetDateTime createdAt;
}
