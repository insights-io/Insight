package com.meemaw.test.testconainers.elasticsearch;

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
    if (!ELASTICSEARCH.isRunning()) {
      ELASTICSEARCH.start();
    }
  }


}
