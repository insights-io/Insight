package com.rebrowse.auth.accounts.model.challenge;

import java.time.Duration;
import javax.ws.rs.core.NewCookie;
import org.apache.commons.lang3.RandomStringUtils;

public final class AuthorizationPwdChallengeSession {

  // Keep for 5 minutes
  public static final int TTL = (int) Duration.ofMinutes(5).toSeconds();
  public static final String COOKIE_NAME = "AuthorizationPwdChallengeSessionId";

  // Number of characters in cookie
  public static final int SIZE = 50;
  private static final String COOKIE_PATH = "/";

  private AuthorizationPwdChallengeSession() {}

  public static NewCookie cookie(String value, String domain) {
    return newCookie(value, domain, TTL);
  }

  public static NewCookie clearCookie(String domain) {
    return newCookie(null, domain, 0);
  }

  private static NewCookie newCookie(String value, String domain, int maxAge) {
    return new NewCookie(COOKIE_NAME, value, COOKIE_PATH, domain, null, maxAge, false);
  }

  public static String newIdentifier() {
    return RandomStringUtils.randomAlphanumeric(SIZE);
  }
}
