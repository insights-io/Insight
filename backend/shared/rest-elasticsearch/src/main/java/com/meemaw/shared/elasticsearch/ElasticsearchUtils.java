package com.meemaw.shared.elasticsearch;

import java.util.Arrays;
import java.util.Optional;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticsearchUtils {

  private static final String NUM_THREADS = "ELASTICSEARCH_NUM_THREADS";
  private static final String HOSTS = "ELASTICSEARCH_HOSTS";
  private static final String DEFAULT_HOSTS = "localhost:9200";
  private static final String DEFAULT_SCHEME = HttpHost.DEFAULT_SCHEME_NAME;

  private ElasticsearchUtils() {}

  /**
   * Helper method to parse array of http hosts from environment.
   *
   * @param name of environment variable
   * @return array of parsed http hosts
   */
  public static HttpHost[] fromEnvironment(String name) {
    String hosts = Optional.ofNullable(System.getenv(name)).orElse(DEFAULT_HOSTS);
    return Arrays.stream(hosts.split(","))
        .map(
            hostname -> {
              String[] split = hostname.split(":");
              return new HttpHost(split[0], Integer.parseInt(split[1], 10), DEFAULT_SCHEME);
            })
        .toArray(HttpHost[]::new);
  }

  /**
   * Helper method to parse array of http hosts from ELASTICSEARCH_NUM_THREADS environment variable.
   *
   * @return array of parsed http hosts
   */
  public static HttpHost[] fromEnvironment() {
    return fromEnvironment(HOSTS);
  }

  /**
   * Helper method to parse number of threads from ELASTICSEARCH_NUM_THREADS environment variable.
   *
   * @return number of threads
   */
  public static int getNumThreads() {
    return Integer.parseInt(Optional.ofNullable(System.getenv(NUM_THREADS)).orElse("10"));
  }

  /**
   * Helper method to construct a rest high level client from environment configurations.
   *
   * @return rest high level client
   */
  public static RestHighLevelClient restClient() {
    HttpHost[] httpHosts = ElasticsearchUtils.fromEnvironment();

    IOReactorConfig reactorConfig =
        IOReactorConfig.custom().setIoThreadCount(ElasticsearchUtils.getNumThreads()).build();

    RestClientBuilder builder =
        RestClient.builder(httpHosts)
            .setHttpClientConfigCallback(
                httpAsyncClientBuilder ->
                    httpAsyncClientBuilder.setDefaultIOReactorConfig(reactorConfig));

    return new RestHighLevelClient(builder);
  }
}
