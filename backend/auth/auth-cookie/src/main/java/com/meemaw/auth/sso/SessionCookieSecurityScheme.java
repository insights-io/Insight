package com.meemaw.auth.sso;

public final class SessionCookieSecurityScheme {

  public static final String NAME = "Session Cookie";
  public static final String DESCRIPTION =
      "Cookie authentication uses HTTP cookies to authenticate client requests and maintain session information.";

  private SessionCookieSecurityScheme() {}
}
