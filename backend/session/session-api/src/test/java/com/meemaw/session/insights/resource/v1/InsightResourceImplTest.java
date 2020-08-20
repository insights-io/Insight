package com.meemaw.session.insights.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.INSIGHT_ORGANIZATION_ID;
import static com.meemaw.test.setup.SsoTestSetupUtils.cookieExpect401;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdmin;
import static io.restassured.RestAssured.given;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.meemaw.useragent.model.UserAgentDTO;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
@TestInstance(Lifecycle.PER_CLASS)
public class InsightResourceImplTest {

  private static OffsetDateTime createdAt;
  private static final AtomicBoolean hasBeenSetup = new AtomicBoolean(false);
  private static final String DISTINCT_PATH = String.join("/", InsightsResource.PATH, "distinct");
  private static final String COUNT_PATH = String.join("/", InsightsResource.PATH, "count");

  @Inject SessionDatasource sessionDatasource;
  @Inject SqlPool sqlPool;

  @Test
  public void get_session_insights_count__should_throw__on_unauthenticated() {
    cookieExpect401(COUNT_PATH, null);
    cookieExpect401(COUNT_PATH, "random");
    cookieExpect401(COUNT_PATH, SsoSession.newIdentifier());
  }

  @Test
  public void get_session_insights_count__should_throw__on_unsupported_fields() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("random", "gte:aba")
        .queryParam("aba", "gtecaba")
        .queryParam("group_by", "another")
        .queryParam("sort_by", "hehe")
        .queryParam("limit", "not_string")
        .get(COUNT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field in search query\",\"random\":\"Unexpected field in search query\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field in group_by query\"},\"sort_by\":{\"ehe\":\"Unexpected field in sort_by query\"}}}}"));
  }

  @Test
  public void get_session_insights_count__should_return_count__on_empty_request() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":5}}"));
  }

  @Test
  public void get_session_insights_count__should_return_count__on_request_with_filters() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .queryParam("location.city", "eq:Maribor")
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":1}}"));
  }

  @Test
  public void get_session_insights_count__should_return_counts__on_group_by_country() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("group_by", "location.countryName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"location.countryName\":\"Canada\"},{\"count\":1,\"location.countryName\":\"Croatia\"},{\"count\":2,\"location.countryName\":\"Slovenia\"},{\"count\":1,\"location.countryName\":\"United States\"}]}"));
  }

  @Test
  public void
      get_session_insights_count__should_return_counts__on_group_by_country_and_continent() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("group_by", "location.countryName,location.continentName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"location.countryName\":\"Canada\",\"location.continentName\":\"North America\"},{\"count\":1,\"location.countryName\":\"Croatia\",\"location.continentName\":\"Europe\"},{\"count\":2,\"location.countryName\":\"Slovenia\",\"location.continentName\":\"Europe\"},{\"count\":1,\"location.countryName\":\"United States\",\"location.continentName\":\"North America\"}]}"));
  }

  @Test
  public void get_session_insights_count__should_return_counts__on_group_by_device() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("group_by", "user_agent.deviceClass")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"user_agent.deviceClass\":\"Desktop\"},{\"count\":4,\"user_agent.deviceClass\":\"Phone\"}]}"));
  }

  @Test
  public void get_session_insights_distinct__should_throw__on_unauthenticated() {
    cookieExpect401(DISTINCT_PATH, null);
    cookieExpect401(DISTINCT_PATH, "random");
    cookieExpect401(DISTINCT_PATH, SsoSession.newIdentifier());
  }

  @Test
  public void get_session_insights_distinct__should_throw__when_no_columns() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(DISTINCT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"on\":\"Required\"}}}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_cities() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("on", "location.city")
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[null,\"Maribor\",\"New York\",\"Otawa\",\"Zagreb\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_continents() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("on", "location.continentName")
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Europe\",\"North America\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_countries() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("on", "location.countryName")
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Canada\",\"Croatia\",\"Slovenia\",\"United States\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_regions() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .queryParam("on", "location.regionName")
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[null,\"Podravska\",\"Washington\"]}"));
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
                                INSIGHT_ORGANIZATION_ID,
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
                                INSIGHT_ORGANIZATION_ID,
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
                                INSIGHT_ORGANIZATION_ID,
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
                                INSIGHT_ORGANIZATION_ID,
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
                                INSIGHT_ORGANIZATION_ID,
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
            .getSession(firstSessionId, INSIGHT_ORGANIZATION_ID)
            .toCompletableFuture()
            .join()
            .get()
            .getCreatedAt();
  }
}
