package com.rebrowse.auth.accounts.model;

import java.time.Duration;
import javax.ws.rs.core.NewCookie;

public final class SsoAuthorizationSession {

  public static final int TTL = (int) Duration.ofMinutes(5).toSeconds();
  public static final String COOKIE_NAME = "SsoAuthorizationSessionId";

  private static final String COOKIE_PATH = "/";

  private SsoAuthorizationSession() {}

  public static NewCookie clearCookie(String domain) {
    return newCookie(null, domain, 0);
  }

  public static NewCookie cookie(String value, String domain) {
    return newCookie(value, domain, TTL);
  }

  private static NewCookie newCookie(String value, String domain, int maxAge) {
    return new NewCookie(COOKIE_NAME, value, COOKIE_PATH, domain, null, maxAge, false);
  }
}
