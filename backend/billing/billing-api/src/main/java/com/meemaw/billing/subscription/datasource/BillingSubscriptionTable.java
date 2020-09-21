package com.meemaw.billing.subscription.datasource;

public final class BillingSubscriptionTable {

  public static final String ID = "id";
  public static final String PLAN = "plan";
  public static final String CUSTOMER_EXTERNAL_ID = "customer_external_id";
  public static final String CUSTOMER_INTERNAL_ID = "customer_internal_id";
  public static final String PRICE_ID = "price_id";
  public static final String CURRENT_PERIOD_ENDS = "current_period_ends";
  public static final String CREATED_AT = "created_at";
  public static final String CANCELLED_AT = "cancelled_at";

  private BillingSubscriptionTable() {}
}
