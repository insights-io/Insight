package com.rebrowse.events.search.indexer;

import java.util.Optional;

public final class KafkaUtils {

  private static final String BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS";
  private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";

  private KafkaUtils() {}

  public static String fromEnvironment(String name) {
    return Optional.ofNullable(System.getenv(name)).orElse(DEFAULT_BOOTSTRAP_SERVERS);
  }

  public static String fromEnvironment() {
    return fromEnvironment(BOOTSTRAP_SERVERS);
  }
}
