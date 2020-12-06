package com.meemaw.test.testconainers.elasticsearch;

import static org.awaitility.Awaitility.await;

import com.meemaw.test.project.ProjectUtils;
import com.meemaw.test.testconainers.TestContainerApiDependency;
import com.meemaw.test.testconainers.api.AbstractApiTestContainer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.testcontainers.containers.Network;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class ElasticsearchTestContainer extends ElasticsearchContainer
    implements TestContainerApiDependency {

  public static final String NETWORK_ALIAS = "elasticsearch";

  private static final String DOCKER_TAG = "docker.elastic.co/elasticsearch/elasticsearch:7.10.0";

  private ElasticsearchTestContainer() {
    super(DOCKER_TAG);
    withNetwork(Network.SHARED).withNetworkAliases(NETWORK_ALIAS);
  }

  public static ElasticsearchTestContainer newInstance() {
    return new ElasticsearchTestContainer();
  }

  public static RestHighLevelClient restHighLevelClient(ElasticsearchTestContainer container) {
    return new RestHighLevelClient(RestClient.builder(container.getHttpHosts()));
  }

  public static String getDockerBaseUri() {
    return String.format("http://%s:9200", NETWORK_ALIAS);
  }

  public RestHighLevelClient restHighLevelClient() {
    return ElasticsearchTestContainer.restHighLevelClient(this);
  }

  public HttpHost getHttpHost() {
    return HttpHost.create(getHttpHostAddress());
  }

  public HttpHost[] getHttpHosts() {
    return new HttpHost[] {getHttpHost()};
  }

  public void cleanup() throws IOException {
    RestHighLevelClient client = restHighLevelClient();
    client.deleteByQuery(
        new DeleteByQueryRequest("_all").setQuery(new MatchAllQueryBuilder()),
        RequestOptions.DEFAULT);

    client.indices().delete(new DeleteIndexRequest("_all"), RequestOptions.DEFAULT);
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () -> {
              long documentCount =
                  client
                      .search(new SearchRequest("_all"), RequestOptions.DEFAULT)
                      .getHits()
                      .getTotalHits()
                      .value;

              return documentCount == 0;
            });
  }

  /** Apply module migrations. */
  public void applyMigrations() {
    Path elasticsearchMigrationsPath = ProjectUtils.getFromModule("migrations", "elasticsearch");
    applyMigrations(elasticsearchMigrationsPath);
  }

  public void applyMigrations(Path migrationsPath) {
    Path absolutePath = migrationsPath.toAbsolutePath();
    if (!Files.exists(migrationsPath)) {
      System.out.printf("[TEST-SETUP]: Skipping applyMigrations from=%s%n", absolutePath);
      return;
    }

    System.out.printf("[TEST-SETUP]: Applying migrations from=%s%n", absolutePath);
    new ElasticsearchMigrationsTestContainer<>(migrationsPath).start();
  }

  @Override
  public void inject(AbstractApiTestContainer<?> apiContainer) {}
}
