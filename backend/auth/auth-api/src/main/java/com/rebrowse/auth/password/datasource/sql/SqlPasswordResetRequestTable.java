package com.rebrowse.auth.password.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.password.datasource.PasswordResetRequestTable;
import com.rebrowse.auth.user.datasource.sql.SqlMfaConfigurationTable;
import com.rebrowse.auth.user.datasource.sql.SqlUserTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlPasswordResetRequestTable {

  public static final Table<?> TABLE = table("auth.password_reset_request");
  public static final Table<?> TABLE_ALIAS = TABLE.as("password_reset_request");

  public static final Field<UUID> TOKEN = field(PasswordResetRequestTable.TOKEN, UUID.class);
  public static final Field<UUID> USER_ID = field(PasswordResetRequestTable.USER_ID, UUID.class);
  public static final Field<String> EMAIL = field(PasswordResetRequestTable.EMAIL, String.class);
  public static final Field<String> REDIRECT =
      field(PasswordResetRequestTable.REDIRECT, String.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(PasswordResetRequestTable.CREATED_AT, OffsetDateTime.class);
  public static final Field<OffsetDateTime> CREATED_AT_ALIAS = tableAliasField(CREATED_AT);

  public static final List<Field<?>> INSERT_FIELDS = List.of(EMAIL, REDIRECT, USER_ID);
  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(TOKEN, CREATED_AT);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  public static final List<Field<?>> TABLE_FIELDS =
      FIELDS.stream().map(SqlPasswordResetRequestTable::tableField).collect(Collectors.toList());

  public static final Field<UUID> TABLE_ALIAS_USER_ID = tableAliasField(USER_ID);

  public static final List<Field<?>> WITH_LOGIN_INFORMATION_FIELDS =
      Stream.concat(
              Stream.concat(TABLE_FIELDS.stream(), SqlUserTable.TABLE_FIELDS.stream()),
              Stream.of(
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.PARAMS),
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.METHOD),
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.CREATED_AT)))
          .collect(Collectors.toUnmodifiableList());

  private SqlPasswordResetRequestTable() {}

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableAliasField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE_ALIAS.getName(), field.getName()));
  }

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE.getName(), field.getName()));
  }
}
