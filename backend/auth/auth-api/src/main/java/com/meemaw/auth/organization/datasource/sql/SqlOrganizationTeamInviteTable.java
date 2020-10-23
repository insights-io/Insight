package com.meemaw.auth.organization.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.auth.organization.datasource.OrganizationTeamInviteTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlOrganizationTeamInviteTable {

  public static final Table<?> TABLE = table("auth.organization_team_invite");

  public static final Field<UUID> TOKEN = field(OrganizationTeamInviteTable.TOKEN, UUID.class);
  public static final Field<String> EMAIL = field(OrganizationTeamInviteTable.EMAIL, String.class);
  public static final Field<String> ORGANIZATION_ID =
      field(OrganizationTeamInviteTable.ORGANIZATION_ID, String.class);
  public static final Field<String> ROLE = field(OrganizationTeamInviteTable.ROLE, String.class);
  public static final Field<UUID> CREATOR_ID =
      field(OrganizationTeamInviteTable.CREATOR_ID, UUID.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(OrganizationTeamInviteTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(CREATOR_ID, EMAIL, ORGANIZATION_ID, ROLE);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(TOKEN, CREATED_AT);

  private SqlOrganizationTeamInviteTable() {}
}
