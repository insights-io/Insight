package com.rebrowse.billing.subscription.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.billing.subscription.datasource.BillingSubscriptionTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class SqlBillingSubscriptionTable {

  public static final Table<?> TABLE = table("billing.subscription");

  public static final Field<String> ID = DSL.field(BillingSubscriptionTable.ID, String.class);
  public static final Field<String> PLAN = field(BillingSubscriptionTable.PLAN, String.class);
  public static final Field<String> CUSTOMER_EXTERNAL_ID =
      field(BillingSubscriptionTable.CUSTOMER_EXTERNAL_ID, String.class);
  public static final Field<String> CUSTOMER_INTERNAL_ID =
      field(BillingSubscriptionTable.CUSTOMER_INTERNAL_ID, String.class);
  public static final Field<String> STATUS = field(BillingSubscriptionTable.STATUS, String.class);
  public static final Field<String> PRICE_ID =
      field(BillingSubscriptionTable.PRICE_ID, String.class);
  public static final Field<Long> CURRENT_PERIOD_START =
      field(BillingSubscriptionTable.CURRENT_PERIOD_START, Long.class);
  public static final Field<Long> CURRENT_PERIOD_END =
      field(BillingSubscriptionTable.CURRENT_PERIOD_END, Long.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(BillingSubscriptionTable.CREATED_AT, OffsetDateTime.class);
  public static final Field<OffsetDateTime> CANCELED_AT =
      field(BillingSubscriptionTable.CANCELED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(
          ID,
          PLAN,
          CUSTOMER_EXTERNAL_ID,
          CUSTOMER_INTERNAL_ID,
          STATUS,
          PRICE_ID,
          CURRENT_PERIOD_START,
          CURRENT_PERIOD_END);

  public static final List<Field<?>> FIELDS =
      Stream.concat(
              Stream.of(CANCELED_AT),
              Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream()))
          .collect(Collectors.toList());

  public static final Map<String, Field<?>> FIELD_MAPPINGS =
      FIELDS.stream().collect(Collectors.toMap(Field::getName, f -> f));

  private SqlBillingSubscriptionTable() {}
}
