package com.meemaw.test.testconainers.kafka;

import org.testcontainers.containers.KafkaContainer;

public class KafkaTestContainer extends KafkaContainer {

  private static final String CONFLUENT_PLATFORM_VERSION = "5.5.0";

  private KafkaTestContainer() {
    super(CONFLUENT_PLATFORM_VERSION);
  }

  public static KafkaTestContainer newInstance() {
    return new KafkaTestContainer();
  }
}
