package com.meemaw.test.testconainers.elasticsearch;

import static org.awaitility.Awaitility.await;

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

import com.meemaw.test.project.ProjectUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class ElasticsearchTestContainer extends ElasticsearchContainer {

  public static final String NETWORK_ALIAS = "elasticsearch";

  private static final String DOCKER_TAG = "docker.elastic.co/elasticsearch/elasticsearch:7.9.3";

  private ElasticsearchTestContainer() {
    super(DOCKER_TAG);
    withNetwork(Network.SHARED);
    withNetworkAliases(NETWORK_ALIAS);
  }

  public static ElasticsearchTestContainer newInstance() {
    return new ElasticsearchTestContainer();
  }

  public static RestHighLevelClient restHighLevelClient(ElasticsearchTestContainer container) {
    return new RestHighLevelClient(RestClient.builder(container.getHttpHosts()));
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

  public void applyMigrations(Path migrationsSqlPath) {
    Path absolutePath = migrationsSqlPath.toAbsolutePath();
    if (!Files.exists(migrationsSqlPath)) {
      System.out.println(
          String.format("[TEST-SETUP]: Skipping applyMigrations from=%s", absolutePath));
      return;
    }

    System.out.println(String.format("[TEST-SETUP]: Applying migrations from=%s", absolutePath));
    new ElasticsearchMigrationsTestContainer<>(migrationsSqlPath).start();
  }
}
