package com.meemaw.auth.sso;

import com.meemaw.auth.sso.oauth.OAuth2Resource;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.shared.rest.response.Boom;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class AbstractIdpService {

  public static final int SECURE_STATE_PREFIX_LENGTH = 26;
  private static final SecureRandom random = new SecureRandom();

  public abstract LoginMethod getLoginMethod();

  public abstract String basePath();

  public String callbackPath() {
    return String.join("/", basePath(), OAuth2Resource.CALLBACK_PATH);
  }

  public String signInPath() {
    return String.join("/", basePath(), OAuth2Resource.SIGNIN_PATH);
  }

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
    try {
      return URLDecoder.decode(
          secureState.substring(SECURE_STATE_PREFIX_LENGTH), StandardCharsets.UTF_8);
    } catch (StringIndexOutOfBoundsException ex) {
      throw Boom.badRequest().message("Invalid state parameter").exception(ex);
    }
  }
}
