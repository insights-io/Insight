package com.meemaw.auth.password.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;

public final class PasswordResetRequestTable {

  public static final Table<?> TABLE = table("auth.password_reset_request");

  public static final Field<UUID> TOKEN = field("token", UUID.class);
  public static final Field<UUID> USER_ID = field("user_id", UUID.class);
  public static final Field<String> EMAIL = field("email", String.class);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS = List.of(EMAIL, USER_ID);
  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(TOKEN, CREATED_AT);

  private PasswordResetRequestTable() {}
}
