package com.meemaw.test.testconainers.kafka;

import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class KafkaTestExtension implements BeforeAllCallback {

  private static final KafkaTestContainer KAFKA = KafkaTestContainer.newInstance();

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
      log.info("[TEST-SETUP]: Starting kafka container ...");
      kafka.start();
      kafka.createTopics();
    }
    log.info("[TEST-SETUP]: Connecting to kafka bootstrap.servers={}", kafka.getBootstrapServers());
    return Collections.singletonMap("kafka.bootstrap.servers", kafka.getBootstrapServers());
  }

  public static void stop() {
    KAFKA.stop();
  }
}
