package com.meemaw.search.indexer;

import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaUtils {

  private static final String BOOTSTRAP_SERVERS = "BOOTSTRAP_SERVERS";
  private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";

  public static String fromEnvironment(String name) {
    return Optional.ofNullable(System.getenv(name)).orElse(DEFAULT_BOOTSTRAP_SERVERS);
  }

  public static String fromEnvironment() {
    return fromEnvironment(BOOTSTRAP_SERVERS);
  }

}
