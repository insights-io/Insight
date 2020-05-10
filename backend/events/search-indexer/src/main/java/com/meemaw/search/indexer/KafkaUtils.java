package com.meemaw.search.indexer;

import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaUtils {

  private final String BOOTSTRAP_SERVERS = "BOOTSTRAP_SERVERS";
  private final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";

  public String fromEnvironment(String name) {
    return Optional.ofNullable(System.getenv(name)).orElse(DEFAULT_BOOTSTRAP_SERVERS);
  }

  public String fromEnvironment() {
    return fromEnvironment(BOOTSTRAP_SERVERS);
  }
}
