package com.meemaw.auth.user.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class UserTable {

  public static final Table<?> TABLE = table("auth.user");

  public static final Field<UUID> ID = field("id", UUID.class);
  public static final Field<String> EMAIL = field("email", String.class);
  public static final Field<String> FULL_NAME = field("full_name", String.class);
  public static final Field<String> ORGANIZATION_ID = field("organization_id", String.class);
  public static final Field<String> ROLE = field("role", String.class);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(ID, CREATED_AT);
  public static final List<Field<?>> INSERT_FIELDS =
      List.of(EMAIL, FULL_NAME, ORGANIZATION_ID, ROLE);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE.getName(), field.getName()));
  }

  private UserTable() {}
}
