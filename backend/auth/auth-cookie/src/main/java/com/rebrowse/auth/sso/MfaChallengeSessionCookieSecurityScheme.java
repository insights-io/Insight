package com.rebrowse.auth.sso;

public final class MfaChallengeSessionCookieSecurityScheme {

  public static final String NAME = "Multi-Factor-Authentication Challenge Session Cookie";
  public static final String DESCRIPTION =
      "Cookie authentication uses HTTP cookies to authenticate client requests and maintain session information.";

  private MfaChallengeSessionCookieSecurityScheme() {}
}
