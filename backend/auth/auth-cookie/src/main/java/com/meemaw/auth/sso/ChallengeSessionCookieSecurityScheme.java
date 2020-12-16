package com.meemaw.auth.sso;

public final class ChallengeSessionCookieSecurityScheme {

  public static final String NAME = "Challenge Session Cookie";
  public static final String DESCRIPTION =
      "Cookie authentication uses HTTP cookies to authenticate client requests and maintain session information.";

  private ChallengeSessionCookieSecurityScheme() {}
}
