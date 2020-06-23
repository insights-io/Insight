package com.meemaw.auth.organization.invite.datasource.pg;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;

public final class TeamInviteTable {

  public static final Table<?> TABLE = table("auth.team_invite");

  public static final Field<UUID> TOKEN = field("token", UUID.class);
  public static final Field<String> EMAIL = field("email", String.class);
  public static final Field<String> ORGANIZATION_ID = field("organization_id", String.class);
  public static final Field<String> ROLE = field("role", String.class);
  public static final Field<UUID> CREATOR_ID = field("creator_id", UUID.class);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(CREATOR_ID, EMAIL, ORGANIZATION_ID, ROLE);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(TOKEN, CREATED_AT);

  private TeamInviteTable() {}
}
