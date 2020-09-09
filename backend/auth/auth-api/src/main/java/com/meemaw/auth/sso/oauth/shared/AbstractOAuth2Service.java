package com.meemaw.auth.sso.oauth.shared;

import com.meemaw.auth.sso.AbstractIdentityProviderService;
import com.meemaw.auth.sso.oauth.model.OAuthError;
import com.meemaw.auth.sso.oauth.model.OAuthUserInfo;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractOAuth2Service<T, U extends OAuthUserInfo, E extends OAuthError>
    extends AbstractIdentityProviderService {

  @Inject SsoService ssoService;

  public abstract URI buildAuthorizationUri(String state, String serverRedirectUri);

  public abstract CompletionStage<SsoLoginResult<?>> oauth2callback(
      String state, String sessionState, String code, String serverRedirectUri);

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

  public CompletionStage<SsoLoginResult<?>> oauth2callback(
      AbstractOAuth2Client<T, U, E> oauthClient,
      String state,
      String sessionState,
      String code,
      String serverRedirectUri) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      log.warn("[AUTH]: OAuth2 state miss-match, session: {}, query: {}", sessionState, state);
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    return oauthClient
        .codeExchange(code, serverRedirectUri)
        .thenCompose(oauthClient::userInfo)
        .thenCompose(
            userInfo -> {
              String fullName = userInfo.getFullName();
              String email = userInfo.getEmail();
              String location = secureStateData(sessionState);
              String cookieDomain = RequestUtils.parseCookieDomain(location);
              MDC.put(LoggingConstants.USER_EMAIL, email);
              log.info("[AUTH]: OAuth2 successfully retrieved user info email={}", email);

              return ssoService
                  .socialLogin(email, fullName, location)
                  .thenApply(
                      loginResult -> {
                        log.info(
                            "[AUTH]: OAuth2 successfully authenticated user email={} location={}",
                            email,
                            location);
                        return new SsoLoginResult<>(loginResult, cookieDomain);
                      });
            });
  }
}
