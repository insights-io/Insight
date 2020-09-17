package com.meemaw.auth.billing.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.auth.billing.datasource.BillingInvoiceTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlBillingInvoiceTable {

  public static final Table<?> TABLE = table("billing.invoice");

  public static final Field<String> ID = field(BillingInvoiceTable.ID, String.class);
  public static final Field<String> CUSTOMER_ID =
      field(BillingInvoiceTable.CUSTOMER_ID, String.class);
  public static final Field<String> SUBSCRIPTION_ID =
      field(BillingInvoiceTable.SUBSCRIPTION_ID, String.class);
  public static final Field<String> ORGANIZATION_ID =
      field(BillingInvoiceTable.ORGANIZATION_ID, String.class);
  public static final Field<String> CURRENCY = field(BillingInvoiceTable.CURRENCY, String.class);
  public static final Field<Long> AMOUNT_PAID = field(BillingInvoiceTable.AMOUNT_PAID, Long.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(BillingInvoiceTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(ID, CUSTOMER_ID, SUBSCRIPTION_ID, ORGANIZATION_ID, CURRENCY, AMOUNT_PAID);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  private SqlBillingInvoiceTable() {}
}
