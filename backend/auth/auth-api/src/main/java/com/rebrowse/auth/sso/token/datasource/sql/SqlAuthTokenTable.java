package com.rebrowse.auth.sso.token.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.sso.token.datasource.AuthTokenTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlAuthTokenTable {

  public static final Table<?> TABLE = table("auth.token");

  public static final Field<String> TOKEN = field(AuthTokenTable.TOKEN, String.class);
  public static final Field<UUID> USER_ID = field(AuthTokenTable.USER_ID, UUID.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(AuthTokenTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS = List.of(TOKEN, USER_ID);
  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  private SqlAuthTokenTable() {}
}
