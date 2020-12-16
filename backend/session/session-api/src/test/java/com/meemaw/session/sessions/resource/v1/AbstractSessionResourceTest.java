package com.meemaw.session.sessions.resource.v1;

import com.meemaw.location.model.Located;
import com.meemaw.location.model.Location;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.query.AbstractQueryParser;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.test.setup.ExternalAuthApiProvidedTest;
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

  protected static final HasUserAgent DESKTOP_MAC_CHROME =
      new UserAgent("Desktop", "Mac OS X", "Chrome");

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
                      DESKTOP_MAC_CHROME,
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
                      new UserAgent("Phone", "Mac OS X", "Chrome"),
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
                      new UserAgent("Phone", "Mac OS X", "Chrome"),
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      Location.builder().countryName("Slovenia").continentName("Europe").build(),
                      new UserAgent("Phone", "Mac OS X", "Chrome"),
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
                      new UserAgent("Phone", "Mac OS X", "Chrome"),
                      transaction)
                  .thenApply(sessions::add);

              return transaction.commit().thenApply(ignored -> null);
            })
        .toCompletableFuture()
        .join();

    return sessions;
  }
}
