package com.rebrowse.test.testconainers.pg;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

/**
 * Quarkus compatible test resource.
 *
 * <p>USAGE: @QuarkusTestResource(PostgresTestResource.class)
 */
public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

  @Override
  public Map<String, String> start() {
    return PostgresTestExtension.start();
  }

  @Override
  public void stop() {
    PostgresTestExtension.stop();
  }
}
