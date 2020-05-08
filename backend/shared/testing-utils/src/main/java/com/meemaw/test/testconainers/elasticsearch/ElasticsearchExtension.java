package com.meemaw.test.testconainers.elasticsearch;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ElasticsearchExtension implements BeforeAllCallback {

  private final static ElasticsearchTestContainer ELASTICSEARCH = ElasticsearchTestContainer
      .newInstance();

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

  public static Map<String, String> start(ElasticsearchTestContainer elasticsearch) {
    if (!ELASTICSEARCH.isRunning()) {
      elasticsearch.start();
    }
    return Collections.emptyMap();
  }

  public static void stop() {
    ELASTICSEARCH.stop();
  }

}
