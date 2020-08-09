package com.meemaw.session.insights.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.INSIGHT_ORGANIZATION_ID;
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

  @Inject SessionDatasource sessionDatasource;
  @Inject SqlPool sqlPool;

  @Test
  public void get_session_insights__should_throw__on_unsupported_fields() {
    String path =
        String.join(
            "/",
            InsightsResource.PATH,
            "count?random=gte:aba&aba=gtecaba&group_by=another&sort_by=hehe&limit=not_string");

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(path)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field in search query\",\"random\":\"Unexpected field in search query\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field in group_by query\"},\"sort_by\":{\"ehe\":\"Unexpected field in sort_by query\"}}}}"));
  }

  @Test
  public void get_session_insights__should_return_count__on_empty_request() {
    String path =
        String.format(
            String.join("/", InsightsResource.PATH, "count?created_at=gte:%s"), createdAt);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(path)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":5}}"));
  }

  @Test
  public void get_session_insights__should_return_counts__on_group_by_country() {
    String path =
        String.format(
            String.join(
                "/",
                InsightsResource.PATH,
                "count?group_by=location.countryName&created_at=gte:%s"),
            createdAt);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(path)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"location.countryName\":\"Canada\"},{\"count\":1,\"location.countryName\":\"Croatia\"},{\"count\":2,\"location.countryName\":\"Slovenia\"},{\"count\":1,\"location.countryName\":\"United States\"}]}"));
  }

  @Test
  public void get_session_insights__should_return_counts__on_group_by_country_and_continent() {
    String path =
        String.format(
            String.join(
                "/",
                InsightsResource.PATH,
                "count?group_by=location.countryName,location.continentName&created_at=gte:%s"),
            createdAt);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(path)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"location.countryName\":\"Canada\",\"location.continentName\":\"North America\"},{\"count\":1,\"location.countryName\":\"Croatia\",\"location.continentName\":\"Europe\"},{\"count\":2,\"location.countryName\":\"Slovenia\",\"location.continentName\":\"Europe\"},{\"count\":1,\"location.countryName\":\"United States\",\"location.continentName\":\"North America\"}]}"));
  }

  @Test
  public void get_session_insights__should_return_counts__on_group_by_device() {
    String path =
        String.format(
            String.join(
                "/",
                InsightsResource.PATH,
                "count?group_by=user_agent.deviceClass&created_at=gte:%s"),
            createdAt);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdmin())
        .get(path)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"user_agent.deviceClass\":\"Desktop\"},{\"count\":4,\"user_agent.deviceClass\":\"Phone\"}]}"));
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
                                    .countryName("United States")
                                    .continentName("North America")
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
