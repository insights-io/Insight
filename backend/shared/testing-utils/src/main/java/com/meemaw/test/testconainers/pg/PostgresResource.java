package com.meemaw.test.testconainers.pg;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {

  private static final PostgresSQLTestContainer POSTGRES = PostgresSQLTestContainer.newInstance();

  @Override
  public Map<String, String> start() {
    POSTGRES.start();
    POSTGRES.applyMigrations();
    return Collections.singletonMap("quarkus.datasource.url", POSTGRES.getDatasourceURL());
  }

  @Override
  public void stop() {
    POSTGRES.stop();
  }

}
