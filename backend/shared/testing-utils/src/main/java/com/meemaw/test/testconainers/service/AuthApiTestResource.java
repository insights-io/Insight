package com.meemaw.test.testconainers.service;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

/**
 * Quarkus compatible test resource.
 *
 * <p>
 * USAGE: @QuarkusTestResource(AuthApiTestResource.class)
 */
public class AuthApiTestResource implements QuarkusTestResourceLifecycleManager {

  private static final AuthApiTestContainer AUTH_API = AuthApiTestContainer.newInstance();

  public static AuthApiTestContainer getInstance() {
    return AUTH_API;
  }

  @Override
  public Map<String, String> start() {
    AUTH_API.start();
    return Map.of(
        "service.auth.host",
        AUTH_API.getContainerIpAddress(),
        "service.auth.port",
        String.valueOf(AUTH_API.getPort()));
  }

  @Override
  public void stop() {
    AUTH_API.stop();
  }
}
