package com.meemaw.session.core.config.resource;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.meemaw.test.testconainers.api.auth.AuthApiTestExtension;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.kafka.KafkaTestExtension;
import com.meemaw.test.testconainers.kafka.KafkaTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestExtension;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
@QuarkusTest
@Tag("integration")
public class AppConfigResourceTest {

  @Test
  public void config() {
    String gitCommitSha = "<GIT_COMMIT_SHA>";
    String datasourceURL = PostgresTestExtension.getInstance().getDatasourceURL();
    String kafkaBootstrapServers = KafkaTestExtension.getInstance().getBootstrapServers();
    String ssoResourceBaseURL = AuthApiTestExtension.getInstance().getBaseURI();

    given()
        .when()
        .get(AppConfigResource.PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"gitCommitSha\":\"%s\",\"datasourceURL\":\"%s\",\"kafkaBootstrapServers\":\"%s\", \"ssoResourceBaseURL\":\"%s\"}",
                    gitCommitSha, datasourceURL, kafkaBootstrapServers, ssoResourceBaseURL)));
  }
}
