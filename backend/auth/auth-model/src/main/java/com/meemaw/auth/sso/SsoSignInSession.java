package com.meemaw.auth.sso;

import javax.ws.rs.core.NewCookie;

public final class SsoSignInSession {

  // Keep for 5 minutes
  public static final int TTL = 60 * 5;
  public static final String COOKIE_NAME = "SsoSessionState";

  private static final String COOKIE_PATH = "/";

  private SsoSignInSession() {}

  public static NewCookie clearCookie(String domain) {
    return newCookie(null, domain, 0);
  }

  public static NewCookie cookie(String sessionId, String domain) {
    return newCookie(sessionId, domain, TTL);
  }

  private static NewCookie newCookie(String value, String domain, int maxAge) {
    return new NewCookie(COOKIE_NAME, value, COOKIE_PATH, domain, null, maxAge, false);
  }
}
