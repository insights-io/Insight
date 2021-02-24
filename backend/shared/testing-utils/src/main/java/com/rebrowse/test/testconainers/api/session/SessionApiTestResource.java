package com.rebrowse.test.testconainers.api.session;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

/**
 * Quarkus compatible test resource.
 *
 * <p>USAGE: @QuarkusTestResource(SessionApiTestResource.class)
 */
public class SessionApiTestResource implements QuarkusTestResourceLifecycleManager {

  @Override
  public Map<String, String> start() {
    return SessionApiTestExtension.start();
  }

  @Override
  public void stop() {
    SessionApiTestExtension.stop();
  }
}
