package com.meemaw.auth.sso.service.google;

import com.meemaw.auth.sso.model.SsoSocialLogin;
import java.net.URI;
import java.util.concurrent.CompletionStage;

public interface SsoGoogleService {

  /**
   * Build a Google authorization request URI.
   *
   * @param state of the request
   * @param redirectURI server oauth2callback redirect URL
   * @return URI
   */
  URI buildAuthorizationURI(String state, String redirectURI);

  /**
   * Generates a secure state with a secure random string of length 26 as a prefix.
   *
   * @param data to be encoded
   * @return String secure state
   */
  String secureState(String data);

  /**
   * OAuth2 callback request handler.
   *
   * @param state of the request
   * @param sessionState state associated with the session (cookie)
   * @param code google authorization code
   * @param redirectURI server oauth2callback redirect URL
   * @return SsoSocialLogin
   */
  CompletionStage<SsoSocialLogin> oauth2callback(
      String state, String sessionState, String code, String redirectURI);
}
