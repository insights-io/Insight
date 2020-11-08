package com.meemaw.billing.invoice.model.dto;

import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class InvoiceDTO {

  String id;
  String subscriptionId;
  String organizationId;
  String currency;
  long amountPaid;
  long amountDue;
  String status;
  String link;
  OffsetDateTime createdAt;
}
