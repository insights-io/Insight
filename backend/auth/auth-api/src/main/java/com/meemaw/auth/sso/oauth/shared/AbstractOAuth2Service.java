package com.meemaw.auth.sso.oauth.shared;

import com.meemaw.auth.sso.oauth.model.OAuthError;
import com.meemaw.auth.sso.oauth.model.OAuthUserInfo;
import com.meemaw.auth.sso.session.model.SsoSocialLogin;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractOAuth2Service<T, U extends OAuthUserInfo, E extends OAuthError> {

  public static final int SECURE_STATE_PREFIX_LENGTH = 26;
  private static final SecureRandom random = new SecureRandom();

  @Inject SsoService ssoService;

  public abstract URI buildAuthorizationUri(String state, String redirectUri);

  public abstract CompletionStage<SsoSocialLogin> oauth2callback(
      String state, String sessionState, String code, String redirectUri);

  /**
   * Create a secure state by prefixing it with an unguessable random string of fixed length. It is
   * used to protect against cross-site request forgery attacks.
   *
   * @param data to be encoded in state
   * @return secure state
   */
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

  public CompletionStage<SsoSocialLogin> oauth2callback(
      AbstractOAuth2Client<T, U, E> oauthClient,
      String state,
      String sessionState,
      String code,
      String redirectUri) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      log.warn("[AUTH]: OpenID state miss-match, session: {}, query: {}", sessionState, state);
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    return oauthClient
        .codeExchange(code, redirectUri)
        .thenCompose(oauthClient::userInfo)
        .thenCompose(
            userInfo -> {
              String fullName = userInfo.getFullName();
              String email = userInfo.getEmail();
              String location = secureStateData(sessionState);
              String cookieDomain = RequestUtils.parseCookieDomain(location);
              MDC.put(LoggingConstants.USER_EMAIL, email);
              log.info("[AUTH]: OpenID successfully retrieved user info email={}", email);

              return ssoService
                  .socialLogin(email, fullName)
                  .thenApply(
                      loginResult -> {
                        log.info(
                            "[AUTH]: OpenID successfully authenticated user email={} location={}",
                            email,
                            location);
                        return new SsoSocialLogin(loginResult, location, cookieDomain);
                      });
            });
  }
}
