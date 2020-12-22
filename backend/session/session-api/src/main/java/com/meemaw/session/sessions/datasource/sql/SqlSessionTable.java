package com.meemaw.session.sessions.datasource.sql;

import static com.meemaw.shared.sql.SQLContext.JSON_OBJECT_DATA_TYPE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.session.sessions.datasource.SessionTable;
import io.vertx.core.json.JsonObject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Field;
import org.jooq.Table;

public final class SqlSessionTable {

  public static final Table<?> TABLE = table("session.session");

  public static final Field<UUID> ID = field(SessionTable.ID, UUID.class);
  public static final Field<String> ORGANIZATION_ID =
      field(SessionTable.ORGANIZATION_ID, String.class);

  /* User agent fields */
  public static final Field<JsonObject> USER_AGENT =
      field(SessionTable.USER_AGENT, JSON_OBJECT_DATA_TYPE);

  public static final Field<String> USER_AGENT__DEVICE_CLASS =
      field(SessionTable.USER_AGENT__DEVICE_CLASS, String.class);
  public static final Field<String> USER_AGENT__DEVICE_NAME =
      field(SessionTable.USER_AGENT__DEVICE_NAME, String.class);
  public static final Field<String> USER_AGENT__DEVICE_BRAND =
      field(SessionTable.USER_AGENT__DEVICE_BRAND, String.class);

  public static final Field<String> USER_AGENT__AGENT_NAME =
      field(SessionTable.USER_AGENT__AGENT_NAME, String.class);
  public static final Field<String> USER_AGENT__AGENT_VERSION =
      field(SessionTable.USER_AGENT__AGENT_VERSION, String.class);

  public static final Field<String> USER_AGENT_OPERATING_SYSTEM_NAME =
      field(SessionTable.USER_AGENT__OPERATING_SYSTEM_NAME, String.class);
  public static final Field<String> USER_AGENT_OPERATING_SYSTEM_VERSION =
      field(SessionTable.USER_AGENT__OPERATING_SYSTEM_VERSION, String.class);

  /* Location fields */
  public static final Field<JsonObject> LOCATION =
      field(SessionTable.LOCATION, JSON_OBJECT_DATA_TYPE);
  public static final Field<String> LOCATION__CITY =
      field(SessionTable.LOCATION__CITY, String.class);
  public static final Field<String> LOCATION__COUNTRY =
      field(SessionTable.LOCATION__COUNTRY, String.class);
  public static final Field<String> LOCATION__CONTINENT =
      field(SessionTable.LOCATION__CONTINENT, String.class);
  public static final Field<String> LOCATION__REGION =
      field(SessionTable.LOCATION__REGION, String.class);
  public static final Field<String> LOCATION_IP = field(SessionTable.LOCATION__IP, String.class);

  public static final Field<UUID> DEVICE_ID = field(SessionTable.DEVICE_ID, UUID.class);
  public static final Field<OffsetDateTime> CREATED_AT =
      field(SessionTable.CREATED_AT, OffsetDateTime.class);

  public static final List<Field<?>> INSERT_FIELDS =
      List.of(ID, DEVICE_ID, ORGANIZATION_ID, LOCATION, USER_AGENT);

  public static final List<Field<?>> AUTO_GENERATED_FIELDS = List.of(CREATED_AT);

  public static final List<Field<?>> FIELDS =
      Stream.concat(INSERT_FIELDS.stream(), AUTO_GENERATED_FIELDS.stream())
          .collect(Collectors.toList());

  public static final Map<String, Field<?>> FIELD_MAPPINGS =
      Stream.concat(
              FIELDS.stream(),
              Stream.of(
                  LOCATION__CITY,
                  LOCATION__COUNTRY,
                  LOCATION__CONTINENT,
                  LOCATION__REGION,
                  LOCATION_IP,
                  USER_AGENT__DEVICE_NAME,
                  USER_AGENT__DEVICE_CLASS,
                  USER_AGENT__DEVICE_BRAND,
                  USER_AGENT__AGENT_NAME,
                  USER_AGENT__AGENT_VERSION,
                  USER_AGENT_OPERATING_SYSTEM_NAME,
                  USER_AGENT_OPERATING_SYSTEM_VERSION))
          .collect(Collectors.toMap(Field::getName, f -> f));

  private SqlSessionTable() {}
}
