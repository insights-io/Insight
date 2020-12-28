package com.meemaw.auth.signup.datasource.sql;

import static com.meemaw.shared.sql.SQLContext.JSON_OBJECT_DATA_TYPE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import io.vertx.core.json.JsonObject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;

public final class SignUpRequestTable {

  public static final Table<?> TABLE = table("auth.sign_up_request");

  public static final Field<UUID> TOKEN = field("token", UUID.class);
  public static final Field<String> EMAIL = field("email", String.class);
  public static final Field<String> HASHED_PASSWORD = field("hashed_password", String.class);
  public static final Field<String> FULL_NAME = field("full_name", String.class);
  public static final Field<String> COMPANY = field("company", String.class);
  public static final Field<JsonObject> PHONE_NUMBER = field("phone_number", JSON_OBJECT_DATA_TYPE);
  public static final Field<String> REFERRER = field("referrer", String.class);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(EMAIL, HASHED_PASSWORD, FULL_NAME, COMPANY, PHONE_NUMBER, REFERRER);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(TOKEN, CREATED_AT);

  private SignUpRequestTable() {}

  @SuppressWarnings({"unchecked"})
  public static <T> Field<T> tableField(Field<T> field) {
    return (Field<T>) field(String.join(".", TABLE.getName(), field.getName()));
  }
}
