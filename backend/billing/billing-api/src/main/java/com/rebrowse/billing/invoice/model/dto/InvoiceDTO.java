package com.rebrowse.billing.invoice.model.dto;

import java.time.OffsetDateTime;
import lombok.Value;

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
