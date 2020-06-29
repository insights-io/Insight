package com.meemaw.test.testconainers.elasticsearch;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class ElasticsearchTestExtension implements BeforeAllCallback {

  private static final ElasticsearchTestContainer ELASTICSEARCH =
      ElasticsearchTestContainer.newInstance();

  public static ElasticsearchTestContainer getInstance() {
    return ELASTICSEARCH;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    start(ELASTICSEARCH).forEach(System::setProperty);
  }

  public static Map<String, String> start() {
    return start(ELASTICSEARCH);
  }

  /**
   * @param elasticsearch test container
   * @return elasticsearch configuration properties
   */
  public static Map<String, String> start(ElasticsearchTestContainer elasticsearch) {
    if (!ELASTICSEARCH.isRunning()) {
      log.info("Starting elasticsearch container ...");
      elasticsearch.start();
      elasticsearch.applyMigrations();
    }
    log.info("Connecting to elasticsearch http.host={}", elasticsearch.getHttpHost());
    return Map.of("elasticsearch.http.host", elasticsearch.getHttpHost().toHostString());
  }

  public static void stop() {
    ELASTICSEARCH.stop();
  }
}
