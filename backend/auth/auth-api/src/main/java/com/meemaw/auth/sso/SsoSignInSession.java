package com.meemaw.auth.sso;

import javax.ws.rs.core.NewCookie;

public final class SsoSignInSession {

  public static final String COOKIE_NAME = "state";

  private SsoSignInSession() {}

  public static NewCookie cookie(String value) {
    return new NewCookie(COOKIE_NAME, value);
  }
}
