package com.rebrowse.session.core.config.resource;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.rebrowse.test.testconainers.api.auth.AuthApiTestExtension;
import com.rebrowse.test.testconainers.api.auth.AuthApiTestResource;
import com.rebrowse.test.testconainers.elasticsearch.ElasticsearchTestExtension;
import com.rebrowse.test.testconainers.elasticsearch.ElasticsearchTestResource;
import com.rebrowse.test.testconainers.kafka.KafkaTestExtension;
import com.rebrowse.test.testconainers.kafka.KafkaTestResource;
import com.rebrowse.test.testconainers.pg.PostgresTestExtension;
import com.rebrowse.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URI;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
@QuarkusTestResource(ElasticsearchTestResource.class)
@QuarkusTest
@Tag("integration")
public class AppConfigResourceTest {

  @Test
  public void config() {
    String gitCommitSha = "<GIT_COMMIT_SHA>";
    String datasourceURL = PostgresTestExtension.getInstance().getDatasourceURL();
    String kafkaBootstrapServers = KafkaTestExtension.getInstance().getBootstrapServers();
    URI authApiBaseUri = AuthApiTestExtension.getInstance().getBaseUri();
    int elasticsearchPort = ElasticsearchTestExtension.getInstance().getHttpHost().getPort();

    given()
        .when()
        .get(AppConfigResource.PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"elasticsearchHttpHost\":[{\"port\":%d,\"schemeName\":\"http\",\"hostName\":\"localhost\"}], \"gitCommitSha\":\"%s\",\"datasourceURL\":\"%s\",\"kafkaBootstrapServers\":\"%s\", \"authApiBaseURL\":\"%s\"}",
                    elasticsearchPort,
                    gitCommitSha,
                    datasourceURL,
                    kafkaBootstrapServers,
                    authApiBaseUri)));
  }
}
