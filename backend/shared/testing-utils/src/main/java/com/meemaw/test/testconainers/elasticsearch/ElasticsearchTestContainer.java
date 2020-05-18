package com.meemaw.test.testconainers.elasticsearch;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class ElasticsearchTestContainer extends ElasticsearchContainer {

  private static final String DOCKER_TAG = "docker.elastic.co/elasticsearch/elasticsearch:7.7.0";

  private ElasticsearchTestContainer() {
    super(DOCKER_TAG);
  }

  public static ElasticsearchTestContainer newInstance() {
    return new ElasticsearchTestContainer();
  }

  public static RestHighLevelClient restHighLevelClient(ElasticsearchTestContainer container) {
    return new RestHighLevelClient(RestClient.builder(container.getHttpHost()));
  }

  public RestHighLevelClient restHighLevelClient() {
    return ElasticsearchTestContainer.restHighLevelClient(this);
  }

  public HttpHost getHttpHost() {
    return HttpHost.create(getHttpHostAddress());
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
}
