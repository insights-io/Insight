package com.meemaw.auth.core.config.resource;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.test.testconainers.pg.PostgresTestExtension;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class AppConfigResourceTest {

  @Test
  public void config() {
    String gitCommitSha = "<GIT_COMMIT_SHA>";
    String datasourceURL = PostgresTestExtension.getInstance().getDatasourceURL();

    given()
        .when()
        .get(AppConfigResource.PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"gitCommitSha\":\"%s\",\"datasourceURL\":\"%s\",\"googleOAuthClientId\":\"237859759623-rfpiq8eo37afp0qc294ioqrjtq17q25h.apps.googleusercontent.com\",\"githubOAuthClientId\":\"210a475f7ac15d91bd3c\",\"githubOAuthClientSecret\":\"37b6374b46ddb1805d956354bb74492728cebf22\"}",
                    gitCommitSha, datasourceURL)));
  }
}
