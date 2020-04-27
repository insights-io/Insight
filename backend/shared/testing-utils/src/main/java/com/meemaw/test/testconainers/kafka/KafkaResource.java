package com.meemaw.test.testconainers.kafka;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;

public class KafkaResource implements QuarkusTestResourceLifecycleManager {

  private static final KafkaTestContainer KAFKA = new KafkaTestContainer();

  @Override
  public Map<String, String> start() {
    KAFKA.start();
    return Collections.singletonMap("kafka.bootstrap.servers", KAFKA.getBootstrapServers());
  }

  @Override
  public void stop() {
    KAFKA.stop();
  }
}