package com.rebrowse.auth.sso.setup.datasource.sql;

import static com.rebrowse.shared.sql.SQLContext.JSON_OBJECT_DATA_TYPE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.sso.setup.datasource.SsoSetupTable;
import io.vertx.core.json.JsonObject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlSsoSetupTable {

  public static final Table<?> TABLE = table("auth.organization_sso_setup");

  public static final Field<String> ORGANIZATION_ID =
      field(SsoSetupTable.ORGANIZATION_ID, String.class);
  public static final Field<String> DOMAIN = field(SsoSetupTable.DOMAIN, String.class);
  public static final Field<String> METHOD = field(SsoSetupTable.METHOD, String.class);
  public static final Field<JsonObject> SAML = field(SsoSetupTable.SAML, JSON_OBJECT_DATA_TYPE);

  public static final Field<OffsetDateTime> CREATED_AT =
      field(SsoSetupTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);
  public static final List<Field<?>> INSERT_FIELDS = List.of(ORGANIZATION_ID, DOMAIN, METHOD, SAML);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  private SqlSsoSetupTable() {}
}
