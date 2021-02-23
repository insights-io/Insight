package com.rebrowse.test.testconainers.elasticsearch;

import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

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

  public static Map<String, String> start(ElasticsearchTestContainer elasticsearch) {
    if (!ELASTICSEARCH.isRunning()) {
      System.out.println("[TEST-SETUP]: Starting elasticsearch container ...");
      elasticsearch.start();
      elasticsearch.applyMigrations();
    }

    System.out.println(
        String.format(
            "[TEST-SETUP]: Connecting to elasticsearch http.host=%s", elasticsearch.getHttpHost()));

    return Map.of("elasticsearch.http.host", elasticsearch.getHttpHost().toHostString());
  }

  public static void stop() {
    ELASTICSEARCH.stop();
  }
}
