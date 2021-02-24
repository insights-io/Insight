package com.rebrowse.test.testconainers.elasticsearch;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

/**
 * Quarkus compatible test resource.
 *
 * <p>USAGE: @QuarkusTestResource(ElasticsearchTestResource.class)
 */
public class ElasticsearchTestResource implements QuarkusTestResourceLifecycleManager {

  @Override
  public Map<String, String> start() {
    return ElasticsearchTestExtension.start();
  }

  @Override
  public void stop() {
    ElasticsearchTestExtension.stop();
  }
}
