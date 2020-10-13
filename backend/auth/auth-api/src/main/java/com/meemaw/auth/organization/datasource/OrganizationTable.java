package com.meemaw.auth.organization.datasource;

import java.util.Set;

public final class OrganizationTable {

  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String PLAN = "plan";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";

  public static final Set<String> UPDATABLE_FIELDS = Set.of(NAME);

  private OrganizationTable() {}
}
