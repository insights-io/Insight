package com.meemaw.auth.billing.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.auth.billing.datasource.BillingCustomerTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlBillingCustomerTable {

  public static final Table<?> TABLE = table("billing.customer");

  public static final Field<String> ID = field(BillingCustomerTable.ID, String.class);
  public static final Field<String> ORGANIZATION_ID =
      field(BillingCustomerTable.ORGANIZATION_ID, String.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(BillingCustomerTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> INSERT_FIELDS = List.of(ID, ORGANIZATION_ID);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  private SqlBillingCustomerTable() {}
}
