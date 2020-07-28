package com.meemaw.session.sessions.datasource.impl;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class PgSessionTable {

  public static final Table<?> TABLE = table("session.session");

  public static final Field<UUID> ID = field("id", UUID.class);
  public static final Field<String> ORGANIZATION_ID = field("organization_id", String.class);
  public static final Field<String> USER_AGENT = field("user_agent", String.class);
  public static final Field<String> LOCATION = field("location", String.class);
  public static final Field<UUID> DEVICE_ID = field("device_id", UUID.class);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(ID, DEVICE_ID, ORGANIZATION_ID, LOCATION, USER_AGENT);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  public static final Map<String, Field<?>> FIELD_MAPPINGS =
      FIELDS.stream().collect(Collectors.toMap(Field::getName, field -> field));

  private PgSessionTable() {}
}
