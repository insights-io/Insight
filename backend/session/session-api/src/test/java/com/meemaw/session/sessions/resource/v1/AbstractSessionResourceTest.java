package com.meemaw.session.sessions.resource.v1;

import com.meemaw.location.model.Location;
import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.test.setup.ExternalAuthApiProvidedTest;
import com.meemaw.useragent.model.UserAgentDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

public class AbstractSessionResourceTest extends ExternalAuthApiProvidedTest {

  protected static final String COUNT_PATH = String.join("/", SessionResource.PATH, "count");

  protected static final Location MOCKED_LOCATION =
      LocationDTO.builder()
          .ip("127.0.0.1")
          .city("Boydton")
          .countryName("United States")
          .regionName("Virginia")
          .continentName("North America")
          .latitude(36.667999267578125)
          .longitude(-78.38899993896484)
          .build();

  protected static final UserAgentDTO MOCKED_USER_AGENT =
      new UserAgentDTO("Desktop", "Mac OS X", "Chrome");

  @Inject SqlPool sqlPool;
  @Inject SessionDatasource sessionDatasource;

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
                      LocationDTO.builder()
                          .city("New York")
                          .countryName("United States")
                          .continentName("North America")
                          .regionName("Washington")
                          .build(),
                      new UserAgentDTO("Desktop", "Mac OS X", "Chrome"),
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      LocationDTO.builder()
                          .city("Otawa")
                          .countryName("Canada")
                          .continentName("North America")
                          .build(),
                      new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      LocationDTO.builder()
                          .city("Maribor")
                          .countryName("Slovenia")
                          .continentName("Europe")
                          .regionName("Podravska")
                          .build(),
                      new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      LocationDTO.builder().countryName("Slovenia").continentName("Europe").build(),
                      new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                      transaction)
                  .thenApply(sessions::add);

              sessionDatasource
                  .createSession(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      organizationId,
                      LocationDTO.builder()
                          .city("Zagreb")
                          .countryName("Croatia")
                          .continentName("Europe")
                          .build(),
                      new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                      transaction)
                  .thenApply(sessions::add);

              return transaction.commit().thenApply(ignored -> null);
            })
        .toCompletableFuture()
        .join();

    return sessions;
  }
}
