package com.rebrowse.auth.organization.datasource.sql;

import static com.rebrowse.shared.sql.SQLContext.JSON_OBJECT_DATA_TYPE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.organization.datasource.OrganizationTable;
import io.vertx.core.json.JsonObject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlOrganizationTable {

  public static final Table<?> TABLE = table("auth.organization");

  public static final Field<String> ID = field(OrganizationTable.ID, String.class);
  public static final Field<String> NAME = field(OrganizationTable.NAME, String.class);
  public static final Field<Boolean> OPEN_MEMBERSHIP =
      field(OrganizationTable.OPEN_MEMBERSHIP, Boolean.class);
  public static final Field<Boolean> ENFORCE_MULTI_FACTOR_AUTHENTICATION =
      field(OrganizationTable.ENFORCE_MULTI_FACTOR_AUTHENTICATION, Boolean.class);
  public static final Field<JsonObject> AVATAR =
      field(OrganizationTable.AVATAR, JSON_OBJECT_DATA_TYPE);
  public static final Field<String> DEFAULT_ROLE =
      field(OrganizationTable.DEFAULT_ROLE, String.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(OrganizationTable.CREATED_AT, OffsetDateTime.class);
  public static final Field<OffsetDateTime> UPDATED_AT =
      field(OrganizationTable.UPDATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS = List.of(ID, NAME);
  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT, UPDATED_AT);
  public static final List<Field<?>> FIELDS =
      Stream.concat(
              Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream()),
              Stream.of(AVATAR, DEFAULT_ROLE, OPEN_MEMBERSHIP, ENFORCE_MULTI_FACTOR_AUTHENTICATION))
          .collect(Collectors.toList());

  public static final Map<String, Field<?>> FIELD_MAPPINGS =
      FIELDS.stream().collect(Collectors.toMap(Field::getName, f -> f));

  private SqlOrganizationTable() {}
}
