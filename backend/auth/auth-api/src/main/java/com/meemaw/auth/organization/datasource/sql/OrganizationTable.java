package com.meemaw.auth.organization.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class OrganizationTable {

  public static final Table<?> TABLE = table("auth.organization");

  public static final Field<String> ID = field("id", String.class);
  public static final Field<String> NAME = field("name", String.class);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);
  public static final Field<OffsetDateTime> UPDATED_AT = field("updated_at", OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS = List.of(ID, NAME);
  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT, UPDATED_AT);
  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  private OrganizationTable() {}
}
