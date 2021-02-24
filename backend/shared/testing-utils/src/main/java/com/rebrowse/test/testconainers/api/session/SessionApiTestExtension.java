package com.rebrowse.test.testconainers.api.session;

import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 *
 * <p>USAGE: {@link SessionApi}
 */
public class SessionApiTestExtension implements BeforeAllCallback {

  private static final SessionApiTestContainer SESSION_API = SessionApiTestContainer.newInstance();

  public static SessionApiTestContainer getInstance() {
    return SESSION_API;
  }

  public static void stop() {
    SESSION_API.stop();
  }

  public static Map<String, String> start() {
    return start(SESSION_API);
  }

  public static Map<String, String> start(SessionApiTestContainer sessionApi) {
    if (!sessionApi.isRunning()) {
      System.out.println("[TEST-SETUP]: Starting session api container ...");
      sessionApi.start();
    }
    System.out.printf("[TEST-SETUP]: Connecting to session-api on=%s%n", sessionApi.getBaseUri());
    return Map.of("session-api/mp-rest/url", sessionApi.getBaseUri().toString());
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    start(SESSION_API).forEach(System::setProperty);
  }
}
