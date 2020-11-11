package com.meemaw.auth.sso;

public enum AuthScheme {
  CHALLENGE_SESSION_COOKIE,
  SSO_SESSION_COOKIE,
  BEARER_TOKEN;

  public static AuthScheme fromSecurityRequirement(String securityRequirement) {
    return switch (securityRequirement) {
      case BearerTokenSecurityScheme.NAME -> AuthScheme.BEARER_TOKEN;
      case SsoSessionCookieSecurityScheme.NAME -> AuthScheme.SSO_SESSION_COOKIE;
      case ChallengeSessionCookieSecurityScheme.NAME -> AuthScheme.CHALLENGE_SESSION_COOKIE;
      default -> throw new IllegalArgumentException(securityRequirement);
    };
  }
}
