package com.meemaw.test.testconainers.pg;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


@Slf4j
public class PostgresExtension implements BeforeAllCallback {

  private static PostgresSQLTestContainer POSTGRES = PostgresSQLTestContainer.newInstance();

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!POSTGRES.isRunning()) {
      POSTGRES.start();
      POSTGRES.applyMigrations();
      System.setProperty("quarkus.datasource.url", POSTGRES.getDatasourceURL());
    }
  }
}
