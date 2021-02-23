package com.rebrowse.auth.signup.datasource.sql;

import static com.rebrowse.shared.sql.SQLContext.JSON_OBJECT_DATA_TYPE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.signup.datasource.SignUpRequestTable;
import io.vertx.core.json.JsonObject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlSignUpRequestTable {

  public static final Table<?> TABLE = table("auth.sign_up_request");

  public static final Field<UUID> TOKEN = field(SignUpRequestTable.TOKEN, UUID.class);
  public static final Field<String> EMAIL = field(SignUpRequestTable.EMAIL, String.class);
  public static final Field<String> HASHED_PASSWORD =
      field(SignUpRequestTable.HASHED_PASSWORD, String.class);
  public static final Field<String> FULL_NAME = field(SignUpRequestTable.FULL_NAME, String.class);
  public static final Field<String> COMPANY = field(SignUpRequestTable.COMPANY, String.class);
  public static final Field<JsonObject> PHONE_NUMBER =
      field(SignUpRequestTable.PHONE_NUMBER, JSON_OBJECT_DATA_TYPE);
  public static final Field<String> REDIRECT = field(SignUpRequestTable.REDIRECT, String.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(SignUpRequestTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(EMAIL, HASHED_PASSWORD, FULL_NAME, COMPANY, PHONE_NUMBER, REDIRECT);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(TOKEN, CREATED_AT);

  private SqlSignUpRequestTable() {}

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE.getName(), field.getName()));
  }
}
