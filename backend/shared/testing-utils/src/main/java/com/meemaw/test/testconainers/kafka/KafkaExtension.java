package com.meemaw.test.testconainers.kafka;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class KafkaExtension implements BeforeAllCallback {

  private static final KafkaTestContainer KAFKA = new KafkaTestContainer();

  public static KafkaTestContainer getInstance() {
    return KAFKA;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    start(KAFKA).forEach(System::setProperty);
  }

  public static Map<String, String> start() {
    return start(KAFKA);
  }

  public static Map<String, String> start(KafkaTestContainer kafka) {
    if (!KAFKA.isRunning()) {
      kafka.start();
    }
    return Collections.singletonMap("kafka.bootstrap.servers", kafka.getBootstrapServers());
  }

  public static void stop() {
    KAFKA.stop();
  }


}
