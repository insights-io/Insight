package com.meemaw.session.pages.datasource.impl;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;

public final class PgPageTable {

  public static final Table<?> TABLE = table("session.page");

  public static final Field<UUID> ID = field("id", UUID.class);
  public static final Field<UUID> SESSION_ID = field("session_id", UUID.class);
  public static final Field<String> ORGANIZATION_ID = field("organization_id", String.class);
  public static final Field<String> DOCTYPE = field("doctype", String.class);
  public static final Field<String> URL = field("url", String.class);
  public static final Field<String> REFERRER = field("referrer", String.class);
  public static final Field<Integer> HEIGHT = field("height", Integer.class);
  public static final Field<Integer> WIDTH = field("width", Integer.class);
  public static final Field<Integer> SCREEN_HEIGHT = field("screen_height", Integer.class);
  public static final Field<Integer> SCREEN_WIDTH = field("screen_width", Integer.class);
  public static final Field<Long> COMPILED_TIMESTAMP = field("compiled_timestamp", Long.class);
  public static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(
          ID,
          SESSION_ID,
          ORGANIZATION_ID,
          DOCTYPE,
          URL,
          REFERRER,
          HEIGHT,
          WIDTH,
          SCREEN_HEIGHT,
          SCREEN_WIDTH,
          COMPILED_TIMESTAMP);

  private PgPageTable() {}
}
