package com.rebrowse.billing.invoice.datasource;

public final class BillingInvoiceTable {

  public static final String ID = "id";
  public static final String SUBSCRIPTION_ID = "subscription_id";
  public static final String CUSTOMER_INTERNAL_ID = "customer_internal_id";
  public static final String CUSTOMER_EXTERNAL_ID = "customer_external_id";
  public static final String PAYMENT_INTENT = "payment_intent";
  public static final String AMOUNT_PAID = "amount_paid";
  public static final String AMOUNT_DUE = "amount_due";
  public static final String STATUS = "status";
  public static final String CURRENCY = "currency";
  public static final String LINK = "link";
  public static final String CREATED_AT = "created_at";

  private BillingInvoiceTable() {}
}
