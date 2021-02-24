package com.rebrowse.beacon.core.config.resource;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.rebrowse.test.testconainers.api.session.SessionApiTestExtension;
import com.rebrowse.test.testconainers.api.session.SessionApiTestResource;
import com.rebrowse.test.testconainers.kafka.KafkaTestExtension;
import com.rebrowse.test.testconainers.kafka.KafkaTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URI;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(SessionApiTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@QuarkusTest
@Tag("integration")
public class AppConfigResourceTest {

  @Test
  public void config() {
    String gitCommitSha = "<GIT_COMMIT_SHA>";
    String kafkaBootstrapServers = KafkaTestExtension.getInstance().getBootstrapServers();
    URI sessionApiBaseUri = SessionApiTestExtension.getInstance().getBaseUri();

    given()
        .when()
        .get(AppConfigResource.PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"gitCommitSha\":\"%s\",\"kafkaBootstrapServers\":\"%s\", \"sessionResourceBaseURL\":\"%s\"}",
                    gitCommitSha, kafkaBootstrapServers, sessionApiBaseUri)));
  }
}
