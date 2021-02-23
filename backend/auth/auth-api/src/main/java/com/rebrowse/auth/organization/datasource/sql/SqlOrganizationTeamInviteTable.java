package com.rebrowse.auth.organization.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.auth.organization.datasource.OrganizationTeamInviteTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public final class SqlOrganizationTeamInviteTable {

  public static final Table<?> TABLE = table("auth.organization_team_invite");

  public static final Field<UUID> TOKEN = DSL.field(OrganizationTeamInviteTable.TOKEN, UUID.class);
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

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  public static final Map<String, Field<?>> FIELD_MAPPINGS =
      FIELDS.stream().collect(Collectors.toMap(Field::getName, f -> f));

  private SqlOrganizationTeamInviteTable() {}
}
