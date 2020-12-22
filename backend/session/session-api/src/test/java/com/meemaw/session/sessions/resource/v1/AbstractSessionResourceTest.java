package com.meemaw.session.sessions.resource.v1;

import com.meemaw.location.model.Located;
import com.meemaw.location.model.Location;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.query.AbstractQueryParser;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.test.setup.ExternalAuthApiProvidedTest;
import com.meemaw.useragent.model.DeviceClass;
import com.meemaw.useragent.model.HasUserAgent;
import com.meemaw.useragent.model.UserAgent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;

public class AbstractSessionResourceTest extends ExternalAuthApiProvidedTest {

  protected static final String COUNT_PATH = String.format("%s/%s", SessionResource.PATH, "count");
  protected static final String DISTINCT_PATH =
      String.format("%s/%s", SessionResource.PATH, "distinct");

  protected static final List<Function<String, String>> CASE_PROVIDERS =
      List.of(v -> v, AbstractQueryParser::camelCase);

  protected static final Located BOYDTON_US_VIRGINIA_NA =
      Location.builder()
          .ip("127.0.0.1")
          .city("Boydton")
          .countryName("United States")
          .regionName("Virginia")
          .continentName("North America")
          .latitude(36.667999267578125)
          .longitude(-78.38899993896484)
          .build();

  protected static final HasUserAgent MAC__SAFARI =
      new UserAgent(
          "Apple Macintosh",
          "Apple",
          DeviceClass.DESKTOP,
          "Mac OS X",
          "10.11.2",
          "Safari",
          "9.0.2");

  protected static final HasUserAgent HTC_ONE_X10__CHROME =
      new UserAgent(
          "HTC ONE X10",
          "HTC",
          DeviceClass.PHONE,
          "Android",
          "6.0",
          "Chrome Webview",
          "61.0.3163.98");

  protected static final HasUserAgent CHROMECAST__CHROME =
      new UserAgent(
          "Google Chromecast",
          "Google",
          DeviceClass.SET_TOP_BOX,
          "Unknown",
          "??",
          "Chrome",
          "31.0.1650.0");

  protected static final HasUserAgent WINDOWS_10__EDGE =
      new UserAgent(
          "Desktop", "Unknown", DeviceClass.DESKTOP, "Windows NT", "10.0", "Edge", "20.??");

  protected static final HasUserAgent GOOGLE_PIXEL_C__CHROME =
      new UserAgent(
          "Google Pixel C",
          "Google",
          DeviceClass.TABLET,
          "Android",
          "7.0",
          "Chrome Webview",
          "52.0.2743.98");

  @Inject protected SqlPool sqlPool;
  @Inject protected SessionDatasource sessionDatasource;

  protected List<SessionDTO> createTestSessions(String organizationId) {
    List<SessionDTO> sessions = new ArrayList<>();

    sqlPool
        .beginTransaction()
        .thenCompose(
            transaction -> {
              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      Location.builder()
                          .city("New York")
                          .countryName("United States")
                          .continentName("North America")
                          .regionName("Washington")
                          .build(),
                      MAC__SAFARI,
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      Location.builder()
                          .city("Otawa")
                          .countryName("Canada")
                          .continentName("North America")
                          .build(),
                      HTC_ONE_X10__CHROME,
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      Location.builder()
                          .city("Maribor")
                          .countryName("Slovenia")
                          .continentName("Europe")
                          .regionName("Podravska")
                          .build(),
                      CHROMECAST__CHROME,
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      Location.builder().countryName("Slovenia").continentName("Europe").build(),
                      WINDOWS_10__EDGE,
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      Location.builder()
                          .city("Zagreb")
                          .countryName("Croatia")
                          .continentName("Europe")
                          .build(),
                      GOOGLE_PIXEL_C__CHROME,
                      transaction)
                  .thenApply(sessions::add);

              return transaction.commit().thenApply(ignored -> null);
            })
        .toCompletableFuture()
        .join();

    return sessions;
  }
}
