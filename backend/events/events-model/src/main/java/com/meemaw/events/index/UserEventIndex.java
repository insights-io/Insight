package com.meemaw.events.index;

import com.meemaw.events.model.Recorded;
import com.meemaw.events.model.incoming.AbstractBrowserEvent;
import java.util.Map;

public final class UserEventIndex {

  public static final String NAME = "events";

  public static final IndexField SESSION_ID =
      new IndexField(UserEventTable.SESSION_ID, Map.of("type", "keyword"));

  public static final IndexField ORGANIZATION_ID =
      new IndexField(UserEventTable.ORGANIZATION_ID, Map.of("type", "keyword"));

  public static final IndexField DEVICE_ID =
      new IndexField(UserEventTable.DEVICE_ID, Map.of("type", "keyword"));

  public static final IndexField PAGE_VISIT_ID =
      new IndexField(UserEventTable.PAGE_VISIT_ID, Map.of("type", "keyword"));

  public static final IndexField EVENT_TIMESTAMP =
      new IndexField(Recorded.TIMESTAMP, Map.of("type", "long"));

  public static final IndexField EVENT_TYPE =
      new IndexField(AbstractBrowserEvent.EVENT_TYPE, Map.of("type", "keyword"));

  public static final IndexField EVENT =
      new IndexField(
          "event",
          Map.of(
              "type",
              "nested",
              "properties",
              Map.of(
                  EVENT_TIMESTAMP.getName(),
                  EVENT_TIMESTAMP.getProperties(),
                  EVENT_TYPE.getName(),
                  EVENT_TYPE.getProperties())));

  public static final Map<String, ?> MAPPING =
      Map.of(
          "properties",
          Map.of(
              SESSION_ID.getName(),
              SESSION_ID.getProperties(),
              ORGANIZATION_ID.getName(),
              ORGANIZATION_ID.getProperties(),
              DEVICE_ID.getName(),
              DEVICE_ID.getProperties(),
              PAGE_VISIT_ID.getName(),
              PAGE_VISIT_ID.getProperties(),
              EVENT.getName(),
              EVENT.getProperties()));

  private UserEventIndex() {}
}
