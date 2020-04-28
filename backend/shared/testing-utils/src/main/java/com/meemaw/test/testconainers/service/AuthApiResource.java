package com.meemaw.test.testconainers.service;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

public class AuthApiResource implements QuarkusTestResourceLifecycleManager {

  private static final AuthApiTestContainer AUTH_API = AuthApiTestContainer.newInstance();

  public static AuthApiTestContainer getInstance() {
    return AUTH_API;
  }

  @Override
  public Map<String, String> start() {
    getInstance().start();
    return Map.of(
        "service.auth.host",
        getInstance().getContainerIpAddress(),
        "service.auth.port",
        String.valueOf(getInstance().getPort()));
  }

  @Override
  public void stop() {
    getInstance().stop();
  }
}
