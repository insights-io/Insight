package com.meemaw.auth.user.datasource.sql;

import static com.meemaw.shared.sql.SQLContext.JSON_OBJECT_DATA_TYPE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import io.vertx.core.json.JsonObject;
import org.jooq.Field;
import org.jooq.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class SqlTfaSetupTable {

  public static final Table<?> TABLE = table("auth.user_tfa_setup");
  public static final Table<?> TABLE_ALIAS = TABLE.as("tfa_setup");

  public static final Field<UUID> USER_ID = field("user_id", UUID.class);
  public static final Field<String> METHOD = field("method", String.class);
  public static final Field<UUID> TABLE_ALIAS_USER_ID = tableAliasField(USER_ID);
  public static final Field<JsonObject> PARAMS = field("params", JSON_OBJECT_DATA_TYPE);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS = List.of(USER_ID, PARAMS, METHOD);

  private SqlTfaSetupTable() {}

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableAliasField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE_ALIAS.getName(), field.getName()));
  }
}
