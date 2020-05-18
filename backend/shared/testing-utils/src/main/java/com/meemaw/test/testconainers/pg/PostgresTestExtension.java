package com.meemaw.test.testconainers.pg;

import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 *
 * <p>USAGE: {@link com.meemaw.test.testconainers.pg.Postgres}
 */
@Slf4j
public class PostgresTestExtension implements BeforeAllCallback {

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

  /**
   * Optionally starts a postgres test container (if it is not already running) and applies
   * migrations to it.
   *
   * @param postgres test container
   * @return map of system properties to be applied
   */
  public static Map<String, String> start(PostgresTestContainer postgres) {
    if (!POSTGRES.isRunning()) {
      log.info("Starting postgres container ...");
      postgres.start();
      postgres.applyMigrations();
    }
    log.info("Connecting to postgres datasource.url={}", postgres.getDatasourceURL());
    return Collections.singletonMap("quarkus.datasource.url", postgres.getDatasourceURL());
  }
}
