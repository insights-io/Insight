package com.rebrowse.test.testconainers.api.auth;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

/**
 * Quarkus compatible test resource.
 *
 * <p>USAGE: @QuarkusTestResource(AuthApiTestResource.class)
 */
public class AuthApiTestResource implements QuarkusTestResourceLifecycleManager {

  @Override
  public Map<String, String> start() {
    return AuthApiTestExtension.start();
  }

  @Override
  public void stop() {
    AuthApiTestExtension.stop();
  }
}
