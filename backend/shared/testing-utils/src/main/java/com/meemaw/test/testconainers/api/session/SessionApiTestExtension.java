package com.meemaw.test.testconainers.api.session;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 *
 * <p>USAGE: {@link SessionApi}
 */
@Slf4j
public class SessionApiTestExtension implements BeforeAllCallback {

  private static final SessionApiTestContainer SESSION_API = SessionApiTestContainer.newInstance();

  public static SessionApiTestContainer getInstance() {
    return SESSION_API;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    start(SESSION_API).forEach(System::setProperty);
  }

  public static void stop() {
    SESSION_API.stop();
  }

  public static Map<String, String> start() {
    return start(SESSION_API);
  }

  /**
   * @param sessionAPI
   * @return map of system properties
   */
  public static Map<String, String> start(SessionApiTestContainer sessionAPI) {
    if (!sessionAPI.isRunning()) {
      log.info("Starting session api container ...");
      sessionAPI.start();
    }
    log.info("Connecting to session api on port {}", sessionAPI.getPort());
    return Map.of(
        "service.session.host",
        sessionAPI.getContainerIpAddress(),
        "service.session.port",
        String.valueOf(sessionAPI.getPort()));
  }
}
