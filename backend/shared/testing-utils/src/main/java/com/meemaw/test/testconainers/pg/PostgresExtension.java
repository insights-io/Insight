package com.meemaw.test.testconainers.pg;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 * <p>
 * USAGE: {@link com.meemaw.test.testconainers.pg.Postgres}
 */
public class PostgresExtension implements BeforeAllCallback, AfterAllCallback {

  private static final PostgresTestContainer POSTGRES = PostgresTestContainer.newInstance();

  public static PostgresTestContainer getInstance() {
    return POSTGRES;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!POSTGRES.isRunning()) {
      start(POSTGRES).forEach(System::setProperty);
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    if (POSTGRES.isRunning()) {
      stop();
    }
  }

  public static void stop() {
    POSTGRES.stop();
  }

  public static Map<String, String> start() {
    return start(POSTGRES);
  }

  public static Map<String, String> start(PostgresTestContainer postgres) {
    postgres.start();
    postgres.applyMigrations();
    return Collections.singletonMap("quarkus.datasource.url", postgres.getDatasourceURL());
  }


}
