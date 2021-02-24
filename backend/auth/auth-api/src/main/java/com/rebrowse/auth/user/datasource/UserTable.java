package com.rebrowse.auth.user.datasource;

import java.util.Map;
import java.util.Set;

public final class UserTable {

  public static final String ID = "id";
  public static final String ORGANIZATION_ID = "organization_id";
  public static final String EMAIL = "email";
  public static final String FULL_NAME = "full_name";
  public static final String PHONE_NUMBER = "phone_number";
  public static final String PHONE_NUMBER_VERIFIED = "phone_number_verified";
  public static final String ROLE = "role";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";

  public static final Set<String> UPDATABLE_FIELDS = Set.of(FULL_NAME, PHONE_NUMBER, ROLE);

  public static final Set<String> QUERYABLE_FIELDS = Set.of(EMAIL, FULL_NAME, ROLE, CREATED_AT);

  private UserTable() {}

  public static final class Errors {

    public static final Map<String, String> PHONE_NUMBER_REQUIRED =
        Map.of(PHONE_NUMBER, "Required");

    public static final Map<String, String> PHONE_NUMBER_VERIFICATION_REQUIRED =
        Map.of(UserTable.PHONE_NUMBER, "Please verify phone number");
  }
}
