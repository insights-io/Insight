package com.meemaw.auth.sso;

public enum AuthScheme {
  SESSION_COOKIE,
  BEARER_TOKEN;

  public static AuthScheme fromSecurityRequirement(String securityRequirement) {
    return switch (securityRequirement) {
      case BearerTokenSecurityScheme.NAME -> AuthScheme.BEARER_TOKEN;
      case SessionCookieSecurityScheme.NAME -> AuthScheme.SESSION_COOKIE;
      default -> throw new IllegalArgumentException(securityRequirement);
    };
  }
}
