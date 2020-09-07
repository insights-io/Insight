package com.meemaw.auth.sso;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class AbstractIdentityProviderService {

  public static final int SECURE_STATE_PREFIX_LENGTH = 26;
  private static final SecureRandom random = new SecureRandom();

  public String secureState(String data) {
    return secureState() + data;
  }

  public String secureState() {
    return RandomStringUtils.random(SECURE_STATE_PREFIX_LENGTH, 0, 0, true, true, null, random);
  }

  /**
   * Extract data encoded in a secure state by stripping the prefix of fixed length.
   *
   * @param secureState from authorization flow
   * @return data that was encoded in the state
   */
  public String secureStateData(String secureState) {
    return URLDecoder.decode(
        secureState.substring(SECURE_STATE_PREFIX_LENGTH), StandardCharsets.UTF_8);
  }
}
