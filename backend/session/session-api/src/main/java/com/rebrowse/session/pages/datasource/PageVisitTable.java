package com.rebrowse.session.pages.datasource;

import java.util.Set;

public final class PageVisitTable {

  public static final String ID = "id";
  public static final String SESSION_ID = "session_id";
  public static final String ORGANIZATION_ID = "organization_id";
  public static final String DOCTYPE = "doctype";
  public static final String ORIGIN = "origin";
  public static final String PATH = "path";
  public static final String REFERRER = "referrer";
  public static final String HEIGHT = "height";
  public static final String WIDTH = "width";
  public static final String SCREEN_HEIGHT = "screen_height";
  public static final String SCREEN_WIDTH = "screen_width";
  public static final String COMPILED_TIMESTAMP = "compiled_timestamp";
  public static final String CREATED_AT = "created_at";

  public static final Set<String> QUERYABLE_FIELDS =
      Set.of(ID, ORGANIZATION_ID, CREATED_AT, REFERRER, ORIGIN, PATH);

  private PageVisitTable() {}
}
