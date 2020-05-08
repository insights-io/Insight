package com.meemaw.test.testconainers.pg;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 * <p>
 * USAGE: {@link com.meemaw.test.testconainers.pg.Postgres}
 */
public class PostgresExtension implements BeforeAllCallback {

  private static final PostgresTestContainer POSTGRES = PostgresTestContainer.newInstance();

  public static PostgresTestContainer getInstance() {
    return POSTGRES;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    start(POSTGRES).forEach(System::setProperty);
  }

  public static void stop() {
    POSTGRES.stop();
  }

  public static Map<String, String> start() {
    return start(POSTGRES);
  }

  public static Map<String, String> start(PostgresTestContainer postgres) {
    if (!POSTGRES.isRunning()) {
      postgres.start();
      postgres.applyMigrations();
    }
    return Collections.singletonMap("quarkus.datasource.url", postgres.getDatasourceURL());
  }


}
