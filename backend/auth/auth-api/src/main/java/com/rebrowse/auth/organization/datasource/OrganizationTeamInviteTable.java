package com.rebrowse.auth.organization.datasource;

import java.util.Set;

public final class OrganizationTeamInviteTable {

  public static final String TOKEN = "token";
  public static final String EMAIL = "email";
  public static final String ORGANIZATION_ID = "organization_id";
  public static final String ROLE = "role";
  public static final String CREATOR_ID = "creator_id";
  public static final String CREATED_AT = "created_at";

  public static final Set<String> QUERYABLE_FIELDS = Set.of(EMAIL, ROLE, CREATED_AT);

  private OrganizationTeamInviteTable() {}
}
