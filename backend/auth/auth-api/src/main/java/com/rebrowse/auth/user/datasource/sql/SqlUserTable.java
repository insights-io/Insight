package com.rebrowse.auth.user.datasource.sql;

import static com.rebrowse.shared.sql.SQLContext.JSON_OBJECT_DATA_TYPE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.password.datasource.sql.SqlPasswordTable;
import com.rebrowse.auth.user.datasource.UserTable;
import io.vertx.core.json.JsonObject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public final class SqlUserTable {

  public static final Table<?> TABLE = table("auth.user");

  public static final Field<UUID> ID = DSL.field(UserTable.ID, UUID.class);
  public static final Field<UUID> USER_TABLE_ID = tableField(ID);
  public static final Field<String> EMAIL = field(UserTable.EMAIL, String.class);
  public static final Field<String> FULL_NAME = field(UserTable.FULL_NAME, String.class);
  public static final Field<JsonObject> PHONE_NUMBER =
      field(UserTable.PHONE_NUMBER, JSON_OBJECT_DATA_TYPE);
  public static final Field<Boolean> PHONE_NUMBER_VERIFIED =
      field(UserTable.PHONE_NUMBER_VERIFIED, Boolean.class);
  public static final Field<String> ORGANIZATION_ID =
      field(UserTable.ORGANIZATION_ID, String.class);
  public static final Field<String> ROLE = field(UserTable.ROLE, String.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(UserTable.CREATED_AT, OffsetDateTime.class);
  public static final Field<OffsetDateTime> UPDATED_AT =
      field(UserTable.UPDATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS =
      List.of(ID, CREATED_AT, UPDATED_AT, PHONE_NUMBER_VERIFIED);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(EMAIL, FULL_NAME, ORGANIZATION_ID, ROLE, PHONE_NUMBER);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  public static final Map<String, Field<?>> FIELD_MAPPINGS =
      FIELDS.stream().collect(Collectors.toMap(Field::getName, f -> f));

  public static final List<Field<?>> TABLE_FIELDS =
      FIELDS.stream().map(SqlUserTable::tableField).collect(Collectors.toList());

  public static final List<Field<?>> WITH_MFA_METHODS_FIELDS =
      Stream.concat(
              TABLE_FIELDS.stream(),
              Stream.of(
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.PARAMS),
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.METHOD),
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.CREATED_AT)))
          .collect(Collectors.toUnmodifiableList());

  public static final List<Field<?>> WITH_LOGIN_INFORMATION_FIELDS =
      Stream.concat(
              TABLE_FIELDS.stream(),
              Stream.of(
                  SqlPasswordTable.tableAliasField(SqlPasswordTable.HASH),
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.PARAMS),
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.METHOD),
                  SqlMfaConfigurationTable.tableAliasField(SqlMfaConfigurationTable.CREATED_AT)))
          .collect(Collectors.toUnmodifiableList());

  private SqlUserTable() {}

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE.getName(), field.getName()));
  }
}
