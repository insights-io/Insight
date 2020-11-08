package com.meemaw.auth.sso;

import org.apache.commons.lang3.RandomStringUtils;

import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.ResponseLoginResult;
import com.meemaw.shared.rest.response.Boom;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

public abstract class AbstractIdentityProvider {

  public static final int SECURE_STATE_PREFIX_LENGTH = 26;
  private static final SecureRandom random = new SecureRandom();

  public abstract LoginMethod getLoginMethod();

  public abstract String basePath();

  public ResponseLoginResult handleSsoException(Throwable throwable, URL redirect) {
    String message = throwable.getCause().getMessage();
    String location =
        UriBuilder.fromUri(URI.create(redirect.toString()))
            .queryParam("oauthError", message)
            .build()
            .toString();

    return new ResponseLoginResult(
        (ignored) -> Response.status(Status.FOUND).header("Location", location).build());
  }

  public String callbackPath() {
    return String.join("/", basePath(), OAuthResource.CALLBACK_PATH);
  }

  public String signInPath() {
    return String.join("/", basePath(), OAuthResource.SIGNIN_PATH);
  }

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
}
