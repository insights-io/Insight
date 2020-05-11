package com.meemaw.test.testconainers.api.auth;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 *
 * <p>USAGE: {@link AuthApi}
 */
@Slf4j
public class AuthApiTestExtension implements BeforeAllCallback {

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

  /**
   * @param authApi
   * @return map of system properties
   */
  public static Map<String, String> start(AuthApiTestContainer authApi) {
    if (!authApi.isRunning()) {
      log.info("Starting auth api container ...");
      authApi.start();
    }
    log.info("Connecting to auth api on port {}", authApi.getPort());
    return Map.of(
        "service.auth.host",
        AUTH_API.getContainerIpAddress(),
        "service.auth.port",
        String.valueOf(AUTH_API.getPort()));
  }
}
