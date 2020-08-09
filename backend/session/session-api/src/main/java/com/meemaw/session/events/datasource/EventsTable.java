package com.meemaw.session.events.datasource;

import java.util.Set;

public final class EventsTable {

  public static final String TYPE = "event.e";
  public static final String TIMESTAMP = "event.t";

  public static final Set<String> QUERYABLE_FIELD = Set.of(TYPE, TIMESTAMP);

  private EventsTable() {}
}
