package com.meemaw.auth.organization.datasource;

import com.meemaw.shared.rest.query.UpdateDTO;
import java.util.Set;

public final class OrganizationTable {

  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String AVATAR = "avatar";
  public static final String OPEN_MEMBERSHIP = "open_membership";
  public static final String DEFAULT_ROLE = "default_role";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";

  public static final Set<String> UPDATABLE_FIELDS =
      UpdateDTO.withCamelCase(Set.of(NAME, DEFAULT_ROLE, OPEN_MEMBERSHIP));

  private OrganizationTable() {}
}
