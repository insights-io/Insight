package com.rebrowse.billing.invoice.model;

import com.stripe.model.Invoice;
import lombok.Value;

@Value
public class CreateBillingInvoiceParams {

  String id;
  String subscriptionId;
  String customerInternalId;
  String customerExternalId;
  String paymentIntent;
  String currency;
  long amountPaid;
  long amountDue;
  String link;
  String status;

  public static CreateBillingInvoiceParams from(Invoice invoice, String organizationId) {
    return new CreateBillingInvoiceParams(
        invoice.getId(),
        invoice.getSubscription(),
        organizationId,
        invoice.getCustomer(),
        invoice.getPaymentIntent(),
        invoice.getCurrency(),
        invoice.getAmountPaid(),
        invoice.getAmountDue(),
        invoice.getHostedInvoiceUrl(),
        invoice.getStatus());
  }
}
