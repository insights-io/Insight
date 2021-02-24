package com.rebrowse.shared.elasticsearch;

import java.util.Arrays;
import java.util.Optional;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public final class ElasticsearchUtils {

  private static final String NUM_THREADS = "ELASTICSEARCH_NUM_THREADS";
  private static final String HOSTS = "ELASTICSEARCH_HOSTS";
  private static final String HOSTS_PROPERTY = "elasticsearch.http.host";
  private static final String DEFAULT_HOSTS = "localhost:9200";
  private static final String DEFAULT_SCHEME = HttpHost.DEFAULT_SCHEME_NAME;

  private ElasticsearchUtils() {}

  /**
   * Helper method to parse array of http hosts from environment.
   *
   * @param name of environment variable
   * @return maybe array of parsed http hosts
   */
  public static Optional<HttpHost[]> fromEnvironment(String name) {
    return Optional.ofNullable(System.getenv(name)).map(ElasticsearchUtils::mapHosts);
  }

  /**
   * Helper method to parse array of http hosts from ELASTICSEARCH_NUM_THREADS environment variable.
   *
   * @return array of parsed http hosts
   */
  public static Optional<HttpHost[]> fromEnvironment() {
    return fromEnvironment(HOSTS);
  }

  public static Optional<HttpHost[]> fromProperty() {
    return Optional.ofNullable(System.getProperty(HOSTS_PROPERTY))
        .map(ElasticsearchUtils::mapHosts);
  }

  private static HttpHost[] mapHosts(String hosts) {
    return Arrays.stream(hosts.split(","))
        .map(
            hostname -> {
              String[] split = hostname.split(":");
              return new HttpHost(split[0], Integer.parseInt(split[1], 10), DEFAULT_SCHEME);
            })
        .toArray(HttpHost[]::new);
  }

  /**
   * Helper method to parse number of threads from ELASTICSEARCH_NUM_THREADS environment variable.
   *
   * @return number of threads
   */
  public static int getNumThreads() {
    return Integer.parseInt(Optional.ofNullable(System.getenv(NUM_THREADS)).orElse("10"));
  }

  public static HttpHost[] httpHosts() {
    return ElasticsearchUtils.fromProperty()
        .orElse(ElasticsearchUtils.fromEnvironment().orElse(mapHosts(DEFAULT_HOSTS)));
  }

  /**
   * Helper method to construct a rest high level client from environment configurations.
   *
   * @return rest high level client
   */
  public static RestHighLevelClient restClient() {
    return restClient(httpHosts());
  }

  public static RestHighLevelClient restClient(HttpHost[] hosts) {
    IOReactorConfig reactorConfig =
        IOReactorConfig.custom().setIoThreadCount(ElasticsearchUtils.getNumThreads()).build();

    RestClientBuilder builder =
        RestClient.builder(hosts)
            .setHttpClientConfigCallback(
                httpAsyncClientBuilder ->
                    httpAsyncClientBuilder.setDefaultIOReactorConfig(reactorConfig));

    return new RestHighLevelClient(builder);
  }
}
