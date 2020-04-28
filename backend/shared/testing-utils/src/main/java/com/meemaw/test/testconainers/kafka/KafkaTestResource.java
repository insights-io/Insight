package com.meemaw.test.testconainers.kafka;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;

/**
 * Quarkus compatible test resource.
 *
 * <p>
 * USAGE: @QuarkusTestResource(KafkaTestResource.class)
 */
public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

  @Override
  public Map<String, String> start() {
    return KafkaExtension.start();
  }

  @Override
  public void stop() {
    KafkaExtension.stop();
  }
}