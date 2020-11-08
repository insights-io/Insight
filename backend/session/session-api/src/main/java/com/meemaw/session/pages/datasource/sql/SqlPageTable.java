package com.meemaw.session.pages.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import org.jooq.Field;
import org.jooq.Table;

import com.meemaw.session.pages.datasource.PageTable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SqlPageTable {

  public static final Table<?> TABLE = table("session.page");

  public static final Field<UUID> ID = field(PageTable.ID, UUID.class);
  public static final Field<UUID> SESSION_ID = field(PageTable.SESSION_ID, UUID.class);
  public static final Field<String> ORGANIZATION_ID =
      field(PageTable.ORGANIZATION_ID, String.class);
  public static final Field<String> DOCTYPE = field(PageTable.DOCTYPE, String.class);
  public static final Field<String> URL = field(PageTable.URL, String.class);
  public static final Field<String> REFERRER = field(PageTable.REFERRER, String.class);
  public static final Field<Integer> HEIGHT = field(PageTable.HEIGHT, Integer.class);
  public static final Field<Integer> WIDTH = field(PageTable.WIDTH, Integer.class);
  public static final Field<Integer> SCREEN_HEIGHT = field(PageTable.SCREEN_HEIGHT, Integer.class);
  public static final Field<Integer> SCREEN_WIDTH = field(PageTable.SCREEN_WIDTH, Integer.class);
  public static final Field<Long> COMPILED_TIMESTAMP =
      field(PageTable.COMPILED_TIMESTAMP, Long.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(PageTable.CREATED_AT, OffsetDateTime.class);

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

  private SqlPageTable() {}
}
