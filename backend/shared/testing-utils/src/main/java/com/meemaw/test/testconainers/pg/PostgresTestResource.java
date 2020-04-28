package com.meemaw.test.testconainers.pg;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

/**
 * Quarkus compatible test resource.
 *
 * <p>
 * USAGE: @QuarkusTestResource(PostgresTestResource.class)
 */
public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

  @Override
  public Map<String, String> start() {
    return PostgresExtension.start();
  }

  @Override
  public void stop() {
    PostgresExtension.stop();
  }

}
