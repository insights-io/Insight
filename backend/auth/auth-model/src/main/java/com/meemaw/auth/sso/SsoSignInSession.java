package com.meemaw.auth.sso;

import javax.ws.rs.core.NewCookie;

public final class SsoSignInSession {

  private static final String COOKIE_PATH = "/";
  public static final String COOKIE_NAME = "SsoSessionState";

  private SsoSignInSession() {}

  public static NewCookie cookie(String value, String domain) {
    return new NewCookie(COOKIE_NAME, value, COOKIE_PATH, domain, null, -1, false);
  }
}
