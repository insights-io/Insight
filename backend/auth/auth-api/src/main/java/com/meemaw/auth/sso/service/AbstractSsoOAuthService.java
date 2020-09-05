package com.meemaw.auth.sso.service;

import com.meemaw.auth.sso.model.OAuthError;
import com.meemaw.auth.sso.model.OAuthUserInfo;
import com.meemaw.auth.sso.model.SsoSocialLogin;
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
public abstract class AbstractSsoOAuthService<T, U extends OAuthUserInfo, E extends OAuthError> {

  public static final int SECURE_STATE_PREFIX_LENGTH = 26;
  private static final SecureRandom random = new SecureRandom();

  @Inject SsoService ssoService;

  public String secureState(String data) {
    String secureString =
        RandomStringUtils.random(SECURE_STATE_PREFIX_LENGTH, 0, 0, true, true, null, random);
    return secureString + data;
  }

  public String secureStateData(String secureState) {
    return URLDecoder.decode(
        secureState.substring(SECURE_STATE_PREFIX_LENGTH), StandardCharsets.UTF_8);
  }

  public abstract URI buildAuthorizationUri(String state, String redirectUri);

  public abstract CompletionStage<SsoSocialLogin> oauth2callback(
      String state, String sessionState, String code, String redirectUri);

  public CompletionStage<SsoSocialLogin> oauth2callback(
      AbstractSsoOAuthClient<T, U, E> oauthClient,
      String state,
      String sessionState,
      String code,
      String redirectUri) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      log.warn("[AUTH]: State miss-match, session: {}, query: {}", sessionState, state);
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
              log.info("[AUTH]: OAuth successfully retrieved user info email={}", email);

              return ssoService
                  .socialLogin(email, fullName)
                  .thenApply(
                      loginResult -> {
                        log.info(
                            "[AUTH]: OAuth successfully authenticated user email={} location={}",
                            email,
                            location);
                        return new SsoSocialLogin(loginResult, location, cookieDomain);
                      });
            });
  }
}
