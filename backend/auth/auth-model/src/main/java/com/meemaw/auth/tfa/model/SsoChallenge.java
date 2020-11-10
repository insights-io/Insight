package com.meemaw.auth.tfa.model;

import javax.ws.rs.core.NewCookie;
import org.apache.commons.lang3.RandomStringUtils;

// TODO: add test for TTL and such
public class SsoChallenge {

  // Keep for 5 minutes
  public static final int TTL = 60 * 5;

  public static final String COOKIE_NAME = "ChallengeId";

  // Number of characters in cookie
  public static final int SIZE = 50;
  private static final String COOKIE_PATH = "/";

  private SsoChallenge() {}

  public static NewCookie cookie(String sessionId, String domain) {
    return newCookie(sessionId, domain, TTL);
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
