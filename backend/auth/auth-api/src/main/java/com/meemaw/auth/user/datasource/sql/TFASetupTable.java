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

public class TFASetupTable {

  public static final Table<?> TABLE = table("auth.user_tfa_setup");
  public static final Table<?> TABLE_ALIAS = TABLE.as("tfa_setup");

  public static final Field<UUID> USER_ID = field("user_id", UUID.class);
  public static final Field<UUID> TABLE_ALIAS_USER_ID = tableAliasField(USER_ID);
  public static final Field<String> SECRET = field("secret", String.class);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS = List.of(USER_ID, SECRET);
  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  private TFASetupTable() {}

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE.getName(), field.getName()));
  }

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableAliasField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE_ALIAS.getName(), field.getName()));
  }
}
