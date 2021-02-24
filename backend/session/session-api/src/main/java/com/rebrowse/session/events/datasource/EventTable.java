package com.rebrowse.session.events.datasource;

import java.util.Set;

public final class EventTable {

  public static final String TYPE = "event.e";
  public static final String TIMESTAMP = "event.t";

  public static final Set<String> QUERYABLE_FIELD = Set.of(TYPE, TIMESTAMP);

  private EventTable() {}
}
