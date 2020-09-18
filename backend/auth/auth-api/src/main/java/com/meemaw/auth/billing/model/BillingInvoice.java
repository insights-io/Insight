package com.meemaw.auth.billing.model;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class BillingInvoice {

  String id;
  String customerId;
  String subscriptionId;
  String organizationId;
  String currency;
  long amountPaid;
  OffsetDateTime createdAt;
}
