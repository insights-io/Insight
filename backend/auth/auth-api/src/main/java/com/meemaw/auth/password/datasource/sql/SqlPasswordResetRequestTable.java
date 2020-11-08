package com.meemaw.auth.password.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import org.jooq.Field;
import org.jooq.Table;

import com.meemaw.auth.password.datasource.PasswordResetRequestTable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SqlPasswordResetRequestTable {

  public static final Table<?> TABLE = table("auth.password_reset_request");

  public static final Field<UUID> TOKEN = field(PasswordResetRequestTable.TOKEN, UUID.class);
  public static final Field<UUID> USER_ID = field(PasswordResetRequestTable.USER_ID, UUID.class);
  public static final Field<String> EMAIL = field(PasswordResetRequestTable.EMAIL, String.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(PasswordResetRequestTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS = List.of(EMAIL, USER_ID);
  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(TOKEN, CREATED_AT);

  private SqlPasswordResetRequestTable() {}
}
