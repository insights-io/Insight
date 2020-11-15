package com.meemaw.test.testconainers.api.auth;

import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 *
 * <p>USAGE: {@link AuthApi}
 */
public class AuthApiTestExtension implements BeforeAllCallback {

  private static final AuthApiTestContainer AUTH_API = AuthApiTestContainer.newInstance();

  public static AuthApiTestContainer getInstance() {
    return AUTH_API;
  }

  public static void stop() {
    AUTH_API.stop();
  }

  public static Map<String, String> start() {
    return start(AUTH_API);
  }

  public static Map<String, String> start(AuthApiTestContainer authApi) {
    if (!authApi.isRunning()) {
      System.out.println("[TEST-SETUP]: Starting auth-api container ...");
      authApi.start();
    }

    String authApiBaseURI = authApi.getBaseURI();
    System.out.printf("[TEST-SETUP]: Connecting to auth-api on=%s%n", authApiBaseURI);

    return Map.of("auth-api/mp-rest/url", authApiBaseURI);
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    start(AUTH_API).forEach(System::setProperty);
  }
}
