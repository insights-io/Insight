package com.meemaw.billing.invoice.model;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class BillingInvoice {

  String id;
  String subscriptionId;
  String paymentIntent;
  String currency;
  long amountPaid;
  long amountDue;
  String status;
  OffsetDateTime createdAt;
}
