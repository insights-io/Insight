package com.rebrowse.session.utils;

import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.query.AbstractQueryParser;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.test.testconainers.api.auth.AuthApiTestResource;
import com.rebrowse.test.utils.auth.AbstractAuthApiProvidedQuarkusTest;
import com.rebrowse.location.model.Located;
import com.rebrowse.location.model.Location;
import com.rebrowse.session.model.PageVisitCreateParams;
import com.rebrowse.session.model.SessionDTO;
import com.rebrowse.session.pages.datasource.PageVisitDatasource;
import com.rebrowse.session.pages.resource.v1.PageVisitResource;
import com.rebrowse.session.sessions.datasource.SessionDatasource;
import com.rebrowse.session.sessions.resource.v1.SessionResource;
import com.rebrowse.useragent.model.DeviceClass;
import com.rebrowse.useragent.model.HasUserAgent;
import com.rebrowse.useragent.model.UserAgent;
import io.quarkus.test.common.QuarkusTestResource;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;

@QuarkusTestResource(AuthApiTestResource.class)
public class AbstractSessionResourceTest extends AbstractAuthApiProvidedQuarkusTest {

  protected static final String PAGES_COUNT_PATH =
      String.format("%s/%s", PageVisitResource.PATH, "count");
  protected static final String SESSION_COUNT_PATH =
      String.format("%s/%s", SessionResource.PATH, "count");
  protected static final String SESSION_DISTINCT_PATH =
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

  protected static final Located OTAWA =
      Location.builder().city("Otawa").countryName("Canada").continentName("North America").build();

  protected static final Located ZAGREB =
      Location.builder().city("Zagreb").countryName("Croatia").continentName("Europe").build();

  protected static final Located MARIBOR =
      Location.builder()
          .city("Maribor")
          .countryName("Slovenia")
          .continentName("Europe")
          .regionName("Podravska")
          .build();

  protected static final Located NEW_YORK =
      Location.builder()
          .city("New York")
          .countryName("United States")
          .continentName("North America")
          .regionName("Washington")
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
  @Inject protected PageVisitDatasource pageVisitDatasource;

  private PageVisitCreateParams pageVisitCreateParams(
      String organizationId, UUID deviceId, URL href, String referrer) {
    return new PageVisitCreateParams(
        organizationId, deviceId, href, referrer, "<!DOCTYPE html>", 100, 100, 100, 100, 100);
  }

  // TODO: Make this better
  protected List<SessionDTO> createTestSessions(String organizationId) {
    List<SessionDTO> sessions = new ArrayList<>();

    sqlPool
        .beginTransaction()
        .thenCompose(
            transaction -> {
              UUID deviceId1 = UUID.randomUUID();
              UUID sessionId1 = UUID.randomUUID();
              sessionDatasource
                  .create(sessionId1, deviceId1, organizationId, NEW_YORK, MAC__SAFARI, transaction)
                  .thenApply(sessions::add);

              UUID deviceId2 = UUID.randomUUID();
              UUID sessionId2 = UUID.randomUUID();
              sessionDatasource
                  .create(
                      sessionId2,
                      deviceId2,
                      organizationId,
                      OTAWA,
                      HTC_ONE_X10__CHROME,
                      transaction)
                  .thenApply(sessions::add);

              UUID deviceId3 = UUID.randomUUID();
              UUID sessionId3 = UUID.randomUUID();
              sessionDatasource
                  .create(
                      sessionId3,
                      deviceId3,
                      organizationId,
                      MARIBOR,
                      CHROMECAST__CHROME,
                      transaction)
                  .thenApply(sessions::add);

              UUID deviceId4 = UUID.randomUUID();
              UUID sessionId4 = UUID.randomUUID();
              sessionDatasource
                  .create(
                      sessionId4,
                      deviceId4,
                      organizationId,
                      Location.builder().countryName("Slovenia").continentName("Europe").build(),
                      WINDOWS_10__EDGE,
                      transaction)
                  .thenApply(sessions::add);

              UUID deviceId5 = UUID.randomUUID();
              UUID sessionId5 = UUID.randomUUID();
              sessionDatasource
                  .create(
                      sessionId5,
                      deviceId5,
                      organizationId,
                      ZAGREB,
                      GOOGLE_PIXEL_C__CHROME,
                      transaction)
                  .thenApply(sessions::add);

              return transaction.commit().thenApply(ignored -> null);
            })
        .toCompletableFuture()
        .join();

    pageVisitDatasource
        .create(
            UUID.randomUUID(),
            sessions.get(0).getId(),
            sessions.get(0).getDeviceId(),
            pageVisitCreateParams(
                organizationId,
                sessions.get(0).getDeviceId(),
                RequestUtils.sneakyUrl("https://localhost:3000/"),
                "https://google.com/"))
        .toCompletableFuture()
        .join();

    pageVisitDatasource
        .create(
            UUID.randomUUID(),
            sessions.get(1).getId(),
            sessions.get(1).getDeviceId(),
            pageVisitCreateParams(
                organizationId,
                sessions.get(1).getDeviceId(),
                RequestUtils.sneakyUrl("https://localhost:3000/"),
                "https://google.com/"))
        .toCompletableFuture()
        .join();

    pageVisitDatasource
        .create(
            UUID.randomUUID(),
            sessions.get(2).getId(),
            sessions.get(2).getDeviceId(),
            pageVisitCreateParams(
                organizationId,
                sessions.get(2).getDeviceId(),
                RequestUtils.sneakyUrl("https://localhost:3000/sessions"),
                "https://facebook.com/"))
        .toCompletableFuture()
        .join();

    pageVisitDatasource
        .create(
            UUID.randomUUID(),
            sessions.get(3).getId(),
            sessions.get(3).getDeviceId(),
            pageVisitCreateParams(
                organizationId,
                sessions.get(3).getDeviceId(),
                RequestUtils.sneakyUrl("https://rebrowse.dev/sessions/"),
                "https://instagram.com/"))
        .toCompletableFuture()
        .join();

    pageVisitDatasource
        .create(
            UUID.randomUUID(),
            sessions.get(4).getId(),
            sessions.get(4).getDeviceId(),
            pageVisitCreateParams(
                organizationId,
                sessions.get(4).getDeviceId(),
                RequestUtils.sneakyUrl("https://rebrowse.dev/settings/"),
                "https://github.com/"))
        .toCompletableFuture()
        .join();

    return sessions;
  }
}
