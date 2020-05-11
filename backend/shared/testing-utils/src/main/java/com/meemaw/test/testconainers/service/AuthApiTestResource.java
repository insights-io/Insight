package com.meemaw.test.testconainers.service;

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
    return AuthApiExtension.start();
  }

  @Override
  public void stop() {
    AuthApiExtension.stop();
  }
}
