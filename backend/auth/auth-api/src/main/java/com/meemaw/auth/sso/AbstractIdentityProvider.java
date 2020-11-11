package com.meemaw.auth.sso;

import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.LoginResult;
import com.meemaw.auth.sso.session.model.ResponseLoginResult;
import com.meemaw.shared.rest.response.Boom;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class AbstractIdentityProvider {

  public static final int SECURE_STATE_PREFIX_LENGTH = 26;
  private static final SecureRandom random = new SecureRandom();

  public static String secureState(String data) {
    return secureState() + data;
  }

  public static String secureState() {
    return RandomStringUtils.random(SECURE_STATE_PREFIX_LENGTH, 0, 0, true, true, null, random);
  }

  /**
   * Extract data encoded in a secure state by stripping the prefix of fixed length.
   *
   * @param secureState from authorization flow
   * @return data that was encoded in the state
   */
  public static String secureStateData(String secureState) {
    try {
      return URLDecoder.decode(
          secureState.substring(SECURE_STATE_PREFIX_LENGTH), StandardCharsets.UTF_8);
    } catch (StringIndexOutOfBoundsException ex) {
      throw Boom.badRequest().message("Invalid state parameter").exception(ex);
    }
  }

  public abstract LoginMethod getLoginMethod();

  public abstract String basePath();

  public LoginResult<?> ssoErrorLoginResult(Throwable throwable, URI redirect) {
    String message = throwable.getCause().getMessage();
    URI location = UriBuilder.fromUri(redirect).queryParam("oauthError", message).build();

    return new ResponseLoginResult(
        (cookieDomain) -> Response.status(Status.FOUND).location(location));
  }

  public String callbackPath() {
    return String.join("/", basePath(), OAuthResource.CALLBACK_PATH);
  }

  public String signInPath() {
    return String.join("/", basePath(), OAuthResource.SIGNIN_PATH);
  }
}
