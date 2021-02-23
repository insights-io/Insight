package com.rebrowse.session.sessions.datasource;

import java.util.Set;

public final class SessionTable {

  public static final String ID = "id";
  public static final String ORGANIZATION_ID = "organization_id";
  public static final String USER_AGENT = "user_agent";

  public static final String USER_AGENT__DEVICE_CLASS =
      String.join(".", USER_AGENT, "device_class");
  public static final String USER_AGENT__DEVICE_NAME = String.join(".", USER_AGENT, "device_name");
  public static final String USER_AGENT__DEVICE_BRAND =
      String.join(".", USER_AGENT, "device_brand");

  public static final String USER_AGENT__AGENT_NAME = String.join(".", USER_AGENT, "agent_name");
  public static final String USER_AGENT__AGENT_VERSION =
      String.join(".", USER_AGENT, "agent_version");

  public static final String USER_AGENT__OPERATING_SYSTEM_NAME =
      String.join(".", USER_AGENT, "operating_system_name");

  public static final String USER_AGENT__OPERATING_SYSTEM_VERSION =
      String.join(".", USER_AGENT, "operating_system_version");

  public static final String LOCATION = "location";
  public static final String LOCATION__COUNTRY = String.join(".", LOCATION, "country_name");
  public static final String LOCATION__CITY = String.join(".", LOCATION, "city");
  public static final String LOCATION__REGION = String.join(".", LOCATION, "region_name");
  public static final String LOCATION__CONTINENT = String.join(".", LOCATION, "continent_name");
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
          USER_AGENT__DEVICE_NAME,
          USER_AGENT__DEVICE_BRAND,
          USER_AGENT__DEVICE_CLASS,
          USER_AGENT__AGENT_NAME,
          USER_AGENT__AGENT_VERSION,
          USER_AGENT__OPERATING_SYSTEM_NAME,
          USER_AGENT__OPERATING_SYSTEM_VERSION);

  private SessionTable() {}
}
