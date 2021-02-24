package com.rebrowse.auth.organization.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.organization.datasource.OrganizationPasswordPolicyTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlOrganizationPasswordPolicyTable {

  public static final Table<?> TABLE = table("auth.organization_password_policy");

  public static final Field<String> ORGANIZATION_ID =
      field(OrganizationPasswordPolicyTable.ORGANIZATION_ID, String.class);
  public static final Field<Short> MIN_CHARACTERS =
      field(OrganizationPasswordPolicyTable.MIN_CHARACTERS, Short.class);
  public static final Field<Boolean> PREVENT_PASSWORD_REUSE =
      field(OrganizationPasswordPolicyTable.PREVENT_PASSWORD_REUSE, Boolean.class);
  public static final Field<Boolean> REQUIRE_UPPERCASE_CHARACTER =
      field(OrganizationPasswordPolicyTable.REQUIRE_UPPERCASE_CHARACTER, Boolean.class);
  public static final Field<Boolean> REQUIRE_LOWERCASE_CHARACTER =
      field(OrganizationPasswordPolicyTable.REQUIRE_LOWERCASE_CHARACTER, Boolean.class);
  public static final Field<Boolean> REQUIRE_NUMBER =
      field(OrganizationPasswordPolicyTable.REQUIRE_NUMBER, Boolean.class);
  public static final Field<Boolean> REQUIRE_NON_ALPHANUMERIC_CHARACTER =
      field(OrganizationPasswordPolicyTable.REQUIRE_NON_ALPHANUMERIC_CHARACTER, Boolean.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(OrganizationPasswordPolicyTable.CREATED_AT, OffsetDateTime.class);
  public static final Field<OffsetDateTime> UPDATED_AT =
      field(OrganizationPasswordPolicyTable.UPDATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(
          ORGANIZATION_ID,
          MIN_CHARACTERS,
          PREVENT_PASSWORD_REUSE,
          REQUIRE_UPPERCASE_CHARACTER,
          REQUIRE_LOWERCASE_CHARACTER,
          REQUIRE_NUMBER,
          REQUIRE_NON_ALPHANUMERIC_CHARACTER);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT, UPDATED_AT);
  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  public static final Map<String, Field<?>> FIELD_MAPPINGS =
      FIELDS.stream().collect(Collectors.toMap(Field::getName, f -> f));

  private SqlOrganizationPasswordPolicyTable() {}
}
