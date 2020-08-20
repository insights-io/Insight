package com.meemaw.session.sessions.datasource;

import java.util.Set;

public final class SessionTable {

  public static final String ID = "id";
  public static final String ORGANIZATION_ID = "organization_id";
  public static final String USER_AGENT = "user_agent";
  public static final String USER_AGENT__DEVICE_CLASS = String.join(".", USER_AGENT, "deviceClass");
  public static final String LOCATION = "location";
  public static final String LOCATION__COUNTRY = String.join(".", LOCATION, "countryName");
  public static final String LOCATION__CITY = String.join(".", LOCATION, "city");
  public static final String LOCATION__REGION = String.join(".", LOCATION, "regionName");
  public static final String LOCATION__CONTINENT = String.join(".", LOCATION, "continentName");
  public static final String LOCATION__IP = String.join(".", LOCATION, "ip");
  public static final String DEVICE_ID = "device_id";
  public static final String CREATED_AT = "created_at";

  public static final Set<String> QUERYABLE_FIELDS =
      Set.of(
          ID,
          ORGANIZATION_ID,
          DEVICE_ID,
          CREATED_AT,
          LOCATION__COUNTRY,
          LOCATION__CONTINENT,
          LOCATION__IP,
          LOCATION__CITY,
          LOCATION__REGION,
          USER_AGENT__DEVICE_CLASS);

  private SessionTable() {}
}
