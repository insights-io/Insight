package com.rebrowse.auth.organization.datasource;

import java.util.Set;

public final class OrganizationPasswordPolicyTable {

  public static final String ORGANIZATION_ID = "organization_id";
  public static final String MIN_CHARACTERS = "min_characters";
  public static final String PREVENT_PASSWORD_REUSE = "prevent_password_reuse";
  public static final String REQUIRE_UPPERCASE_CHARACTER = "require_uppercase_character";
  public static final String REQUIRE_LOWERCASE_CHARACTER = "require_lowercase_character";
  public static final String REQUIRE_NUMBER = "require_number";
  public static final String REQUIRE_NON_ALPHANUMERIC_CHARACTER =
      "require_non_alphanumeric_character";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";

  public static final Set<String> UPDATABLE_FIELDS =
      Set.of(
          MIN_CHARACTERS,
          PREVENT_PASSWORD_REUSE,
          REQUIRE_UPPERCASE_CHARACTER,
          REQUIRE_LOWERCASE_CHARACTER,
          REQUIRE_NUMBER,
          REQUIRE_NON_ALPHANUMERIC_CHARACTER);

  private OrganizationPasswordPolicyTable() {}
}
