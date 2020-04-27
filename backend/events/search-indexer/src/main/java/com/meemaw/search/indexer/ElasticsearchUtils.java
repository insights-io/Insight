package com.meemaw.search.indexer;

import java.util.Arrays;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpHost;

@UtilityClass
public class ElasticsearchUtils {

  private static final String HOSTS = "ELASTICSEARCH_HOSTS";
  private static final String DEFAULT_HOSTS = "localhost:9200";
  private static final String DEFAULT_SCHEME = HttpHost.DEFAULT_SCHEME_NAME;

  public HttpHost[] fromEnvironment(String name) {
    String hosts = Optional.ofNullable(System.getenv(name)).orElse(DEFAULT_HOSTS);
    return Arrays.stream(hosts.split(","))
        .map(hostname -> {
          String[] split = hostname.split(":");
          return new HttpHost(split[0], Integer.parseInt(split[1], 10), DEFAULT_SCHEME);
        })
        .toArray(HttpHost[]::new);
  }

  public HttpHost[] fromEnvironment() {
    return fromEnvironment(HOSTS);
  }

}
