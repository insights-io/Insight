package com.meemaw.auth.sso.tfa.challenge.model;

import javax.ws.rs.core.NewCookie;
import org.apache.commons.lang3.RandomStringUtils;

public class SsoChallenge {

  private static final String COOKIE_PATH = "/";

  public static final String COOKIE_NAME = "ChallengeId";

  private static final int SECONDS_IN_DAY = 60 * 60 * 24;

  // Keep for 365 days
  public static final int TTL = SECONDS_IN_DAY * 365;

  // Number of characters in cookie
  public static final int SIZE = 50;

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