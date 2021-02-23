package com.rebrowse.auth.core.config.resource;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.rebrowse.test.testconainers.pg.PostgresTestExtension;
import com.rebrowse.test.testconainers.pg.PostgresTestResource;
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
                    "{\"gitCommitSha\":\"%s\",\"datasourceURL\":\"%s\",\"googleOpenIdClientId\":\"237859759623-rfpiq8eo37afp0qc294ioqrjtq17q25h.apps.googleusercontent.com\",\"githubOpenIdClientId\":\"210a475f7ac15d91bd3c\",\"microsoftOpenIdClientId\":\"783370b6-ee5d-47b5-bc12-2b9ebe4a4f1b\"}",
                    gitCommitSha, datasourceURL)));
  }
}
