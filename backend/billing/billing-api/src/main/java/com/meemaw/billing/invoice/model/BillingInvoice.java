package com.meemaw.billing.invoice.model;

import lombok.Value;

import com.meemaw.billing.invoice.model.dto.InvoiceDTO;

import java.time.OffsetDateTime;

@Value
public class BillingInvoice {

  String id;
  String subscriptionId;
  String customerInternalId;
  String customerExternalId;
  String paymentIntent;
  String currency;
  long amountPaid;
  long amountDue;
  String status;
  String link;
  OffsetDateTime createdAt;

  public InvoiceDTO dto() {
    return new InvoiceDTO(
        id,
        subscriptionId,
        customerInternalId,
        currency,
        amountPaid,
        amountDue,
        status,
        link,
        createdAt);
  }
}
