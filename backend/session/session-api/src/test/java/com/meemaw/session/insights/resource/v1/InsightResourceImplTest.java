package com.meemaw.session.insights.resource.v1;

import static com.meemaw.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.RestAssuredUtils.ssoBearerTokenTestCases;
import static com.meemaw.test.setup.RestAssuredUtils.ssoSessionCookieTestCases;
import static io.restassured.RestAssured.given;

import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.test.setup.ExternalAuthApiProvidedTest;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.meemaw.useragent.model.UserAgentDTO;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Method;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
public class InsightResourceImplTest extends ExternalAuthApiProvidedTest {

  private static final AtomicBoolean hasBeenSetup = new AtomicBoolean(false);
  private static final String DISTINCT_PATH = String.join("/", InsightsResource.PATH, "distinct");
  private static OffsetDateTime createdAt;

  @Inject SessionDatasource sessionDatasource;
  @Inject SqlPool sqlPool;

  @Test
  public void get_session_insights_distinct__should_throw__on_unauthorized() {
    ssoSessionCookieTestCases(Method.GET, DISTINCT_PATH);
    ssoBearerTokenTestCases(Method.GET, DISTINCT_PATH);
  }

  @Test
  public void get_session_insights_distinct__should_throw__when_no_columns() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(DISTINCT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"on\":\"Required\"}}}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_cities() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("on", "location.city")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Maribor\",\"New York\",\"Otawa\",\"Zagreb\"]}"));

    String apiKey = authApi().createApiKey(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .queryParam("on", "location.city")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Maribor\",\"New York\",\"Otawa\",\"Zagreb\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_continents() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "location.continentName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Europe\",\"North America\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_countries() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "location.countryName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Canada\",\"Croatia\",\"Slovenia\",\"United States\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_regions() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "location.regionName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Podravska\",\"Washington\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_browser_name() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "user_agent.browserName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Chrome\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_operating_system_name() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "user_agent.operatingSystemName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Mac OS X\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_device_class() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "user_agent.deviceClass")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Desktop\",\"Phone\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_throw__when_unexpected_fields() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "random")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"random\":\"Unexpected field\"}}}"));
  }

  @BeforeEach
  void init() {
    if (hasBeenSetup.getAndSet(true)) {
      return;
    }

    UUID firstSessionId = UUID.randomUUID();

    sqlPool
        .beginTransaction()
        .thenCompose(
            transaction ->
                CompletableFuture.allOf(
                        sessionDatasource
                            .createSession(
                                firstSessionId,
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .city("New York")
                                    .countryName("United States")
                                    .continentName("North America")
                                    .regionName("Washington")
                                    .build(),
                                new UserAgentDTO("Desktop", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture(),
                        sessionDatasource
                            .createSession(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .city("Otawa")
                                    .countryName("Canada")
                                    .continentName("North America")
                                    .build(),
                                new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture(),
                        sessionDatasource
                            .createSession(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .city("Maribor")
                                    .countryName("Slovenia")
                                    .continentName("Europe")
                                    .regionName("Podravska")
                                    .build(),
                                new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture(),
                        sessionDatasource
                            .createSession(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .countryName("Slovenia")
                                    .continentName("Europe")
                                    .build(),
                                new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture(),
                        sessionDatasource
                            .createSession(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .city("Zagreb")
                                    .countryName("Croatia")
                                    .continentName("Europe")
                                    .build(),
                                new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture())
                    .thenCompose(ignored -> transaction.commit()))
        .toCompletableFuture()
        .join();

    createdAt =
        sessionDatasource
            .getSession(firstSessionId, REBROWSE_ORGANIZATION_ID)
            .toCompletableFuture()
            .join()
            .get()
            .getCreatedAt();
  }
}
