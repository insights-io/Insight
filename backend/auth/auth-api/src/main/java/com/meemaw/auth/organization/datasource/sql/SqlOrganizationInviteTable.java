package com.meemaw.auth.organization.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.auth.organization.datasource.OrganizationInviteTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlOrganizationInviteTable {

  public static final Table<?> TABLE = table("auth.organization_invite");

  public static final Field<UUID> TOKEN = field(OrganizationInviteTable.TOKEN, UUID.class);
  public static final Field<String> EMAIL = field(OrganizationInviteTable.EMAIL, String.class);
  public static final Field<String> ORGANIZATION_ID =
      field(OrganizationInviteTable.ORGANIZATION_ID, String.class);
  public static final Field<String> ROLE = field(OrganizationInviteTable.ROLE, String.class);
  public static final Field<UUID> CREATOR_ID =
      field(OrganizationInviteTable.CREATOR_ID, UUID.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(OrganizationInviteTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(CREATOR_ID, EMAIL, ORGANIZATION_ID, ROLE);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(TOKEN, CREATED_AT);

  private SqlOrganizationInviteTable() {}
}
