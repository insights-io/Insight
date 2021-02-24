package com.rebrowse.billing.invoice.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.billing.invoice.datasource.BillingInvoiceTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlBillingInvoiceTable {

  public static final Table<?> TABLE = table("billing.invoice");

  public static final Field<String> ID = field(BillingInvoiceTable.ID, String.class);
  public static final Field<String> SUBSCRIPTION_ID =
      field(BillingInvoiceTable.SUBSCRIPTION_ID, String.class);
  public static final Field<String> CUSTOMER_INTERNAL_ID =
      field(BillingInvoiceTable.CUSTOMER_INTERNAL_ID, String.class);
  public static final Field<String> CUSTOMER_EXTERNAL_ID =
      field(BillingInvoiceTable.CUSTOMER_EXTERNAL_ID, String.class);
  public static final Field<String> PAYMENT_INTENT =
      field(BillingInvoiceTable.PAYMENT_INTENT, String.class);
  public static final Field<String> CURRENCY = field(BillingInvoiceTable.CURRENCY, String.class);
  public static final Field<Long> AMOUNT_PAID = field(BillingInvoiceTable.AMOUNT_PAID, Long.class);
  public static final Field<Long> AMOUNT_DUE = field(BillingInvoiceTable.AMOUNT_DUE, Long.class);
  public static final Field<String> STATUS = field(BillingInvoiceTable.STATUS, String.class);
  public static final Field<String> LINK = field(BillingInvoiceTable.LINK, String.class);

  public static final Field<OffsetDateTime> CREATED_AT =
      field(BillingInvoiceTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(
          ID,
          SUBSCRIPTION_ID,
          CUSTOMER_INTERNAL_ID,
          CUSTOMER_EXTERNAL_ID,
          PAYMENT_INTENT,
          CURRENCY,
          AMOUNT_PAID,
          AMOUNT_DUE,
          STATUS,
          LINK);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  private SqlBillingInvoiceTable() {}
}
