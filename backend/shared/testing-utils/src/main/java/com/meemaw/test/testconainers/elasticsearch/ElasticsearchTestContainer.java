package com.meemaw.test.testconainers.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class ElasticsearchTestContainer extends ElasticsearchContainer {

  private static final String DOCKER_TAG = "docker.elastic.co/elasticsearch/elasticsearch:7.6.2";

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
}
