package com.rebrowse.billing.customer.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.billing.customer.datasource.BillingCustomerTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlBillingCustomerTable {

  public static final Table<?> TABLE = table("billing.customer");

  public static final Field<String> EXTERNAL_ID =
      field(BillingCustomerTable.EXTERNAL_ID, String.class);
  public static final Field<String> INTERNAL_ID =
      field(BillingCustomerTable.INTERNAL_ID, String.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(BillingCustomerTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> INSERT_FIELDS = List.of(EXTERNAL_ID, INTERNAL_ID);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  private SqlBillingCustomerTable() {}
}
