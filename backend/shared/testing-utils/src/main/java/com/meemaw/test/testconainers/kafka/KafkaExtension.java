package com.meemaw.test.testconainers.kafka;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class KafkaExtension implements BeforeAllCallback {

  private static final KafkaTestContainer KAFKA = new KafkaTestContainer();

  public static KafkaTestContainer getInstance() {
    return KAFKA;
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    if (!KAFKA.isRunning()) {
      KAFKA.start();
    }
  }
}
