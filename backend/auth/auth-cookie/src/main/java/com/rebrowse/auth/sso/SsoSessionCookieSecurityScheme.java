package com.rebrowse.auth.sso;

public final class SsoSessionCookieSecurityScheme {

  public static final String NAME = "SSO Session Cookie";
  public static final String DESCRIPTION =
      "Cookie authentication uses HTTP cookies to authenticate client requests and maintain session information.";

  private SsoSessionCookieSecurityScheme() {}
}
