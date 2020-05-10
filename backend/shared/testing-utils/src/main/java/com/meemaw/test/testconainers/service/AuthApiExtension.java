package com.meemaw.test.testconainers.service;

import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 * <p>
 * USAGE: {@link com.meemaw.test.testconainers.service.AuthApi}
 */
public class AuthApiExtension implements BeforeAllCallback {

  private static final AuthApiTestContainer AUTH_API = AuthApiTestContainer.newInstance();

  public static AuthApiTestContainer getInstance() {
    return AUTH_API;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    start(AUTH_API).forEach(System::setProperty);
  }

  public static void stop() {
    AUTH_API.stop();
  }

  public static Map<String, String> start() {
    return start(AUTH_API);
  }

  public static Map<String, String> start(AuthApiTestContainer authApi) {
    if (!authApi.isRunning()) {
      authApi.start();
    }
    return Map.of(
        "service.auth.host",
        AUTH_API.getContainerIpAddress(),
        "service.auth.port",
        String.valueOf(AUTH_API.getPort()));
  }

}
