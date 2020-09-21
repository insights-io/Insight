package com.meemaw.billing.subscription.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.billing.subscription.datasource.BillingSubscriptionTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public class SqlBillingSubscriptionTable {

  public static final Table<?> TABLE = table("billing.subscription");

  public static final Field<String> ID = field(BillingSubscriptionTable.ID, String.class);
  public static final Field<String> PLAN = field(BillingSubscriptionTable.PLAN, String.class);
  public static final Field<String> CUSTOMER_EXTERNAL_ID =
      field(BillingSubscriptionTable.CUSTOMER_EXTERNAL_ID, String.class);
  public static final Field<String> CUSTOMER_INTERNAL_ID =
      field(BillingSubscriptionTable.CUSTOMER_INTERNAL_ID, String.class);
  public static final Field<String> PRICE_ID =
      field(BillingSubscriptionTable.PRICE_ID, String.class);
  public static final Field<Long> CURRENT_PERIOD_ENDS =
      field(BillingSubscriptionTable.CURRENT_PERIOD_ENDS, Long.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(BillingSubscriptionTable.CREATED_AT, OffsetDateTime.class);
  public static final Field<OffsetDateTime> CANCELLED_AT =
      field(BillingSubscriptionTable.CANCELLED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(ID, PLAN, CUSTOMER_EXTERNAL_ID, CUSTOMER_INTERNAL_ID, PRICE_ID, CURRENT_PERIOD_ENDS);

  public static final List<Field<?>> FIELDS =
      Stream.concat(
              Stream.of(CANCELLED_AT),
              Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream()))
          .collect(Collectors.toList());

  private SqlBillingSubscriptionTable() {}
}
