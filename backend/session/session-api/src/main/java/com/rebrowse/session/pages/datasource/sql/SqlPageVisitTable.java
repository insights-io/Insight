package com.rebrowse.session.pages.datasource.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.rebrowse.session.pages.datasource.PageVisitTable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlPageVisitTable {

  public static final Table<?> TABLE = table("session.page_visit");

  public static final Field<UUID> ID = field(PageVisitTable.ID, UUID.class);
  public static final Field<UUID> SESSION_ID = field(PageVisitTable.SESSION_ID, UUID.class);
  public static final Field<String> ORGANIZATION_ID =
      field(PageVisitTable.ORGANIZATION_ID, String.class);
  public static final Field<String> DOCTYPE = field(PageVisitTable.DOCTYPE, String.class);
  public static final Field<String> ORIGIN = field(PageVisitTable.ORIGIN, String.class);
  public static final Field<String> PATH = field(PageVisitTable.PATH, String.class);
  public static final Field<String> REFERRER = field(PageVisitTable.REFERRER, String.class);
  public static final Field<Integer> HEIGHT = field(PageVisitTable.HEIGHT, Integer.class);
  public static final Field<Integer> WIDTH = field(PageVisitTable.WIDTH, Integer.class);
  public static final Field<Integer> SCREEN_HEIGHT =
      field(PageVisitTable.SCREEN_HEIGHT, Integer.class);
  public static final Field<Integer> SCREEN_WIDTH =
      field(PageVisitTable.SCREEN_WIDTH, Integer.class);
  public static final Field<Long> COMPILED_TIMESTAMP =
      field(PageVisitTable.COMPILED_TIMESTAMP, Long.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(PageVisitTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(
          ID,
          SESSION_ID,
          ORGANIZATION_ID,
          DOCTYPE,
          ORIGIN,
          PATH,
          REFERRER,
          HEIGHT,
          WIDTH,
          SCREEN_HEIGHT,
          SCREEN_WIDTH,
          COMPILED_TIMESTAMP);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  public static final Map<String, Field<?>> FIELD_MAPPINGS =
      FIELDS.stream().collect(Collectors.toMap(Field::getName, f -> f));

  private SqlPageVisitTable() {}
}
