package com.meemaw.auth.sso.model;

import com.meemaw.auth.sso.model.dto.VerificationResponseDTO;
import com.meemaw.shared.rest.response.DataResponse;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.RandomStringUtils;

public class SsoVerification {

  private static final String COOKIE_PATH = "/";

  public static final String COOKIE_NAME = "VerificationId";

  private static final int SECONDS_IN_DAY = 60 * 60 * 24;

  // Keep for 365 days
  public static final int TTL = SECONDS_IN_DAY * 365;

  // Number of characters in cookie
  public static final int SIZE = 50;

  private SsoVerification() {}

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

  public static Response cookieResponse(String value, String cookieDomain) {
    return DataResponse.okBuilder(new VerificationResponseDTO(value))
        .cookie(SsoVerification.cookie(value, cookieDomain))
        .build();
  }
}
