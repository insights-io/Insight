package com.rebrowse.auth.password.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.password.datasource.PasswordTable;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlPasswordTable {

  public static final Table<?> TABLE = table("auth.password");
  public static final Table<?> TABLE_ALIAS = table("auth.password").as("password");

  public static final Field<UUID> USER_ID = field(PasswordTable.USER_ID, UUID.class);
  public static final Field<UUID> TABLE_ALIAS_USER_ID = tableAliasField(USER_ID);
  public static final Field<String> HASH = field(PasswordTable.HASH, String.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(PasswordTable.CREATED_AT, OffsetDateTime.class);
  public static final Field<OffsetDateTime> TABLE_ALIAS_CREATED_AT = tableAliasField(CREATED_AT);

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableAliasField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE_ALIAS.getName(), field.getName()));
  }

  private SqlPasswordTable() {}
}
