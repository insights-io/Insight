package com.meemaw.auth.sso.oauth.shared;

import com.meemaw.auth.sso.AbstractIdpService;
import com.meemaw.auth.sso.oauth.model.OAuthError;
import com.meemaw.auth.sso.oauth.model.OAuthUserInfo;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.sso.setup.model.SsoSetupDTO;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractOAuth2Service<T, U extends OAuthUserInfo, E extends OAuthError>
    extends AbstractIdpService {

  @Inject SsoService ssoService;

  public abstract URI buildAuthorizationURI(String state, String serverRedirect);

  public abstract CompletionStage<SsoLoginResult<?>> oauth2callback(
      String state, String sessionState, String code, String serverBaseURL);

  @Override
  public String callbackPath() {
    return String.join(
        "/", OAuth2Resource.PATH, getLoginMethod().getKey(), OAuth2Resource.CALLBACK_PATH);
  }

  @Override
  public URI buildAuthorizationURI(String state, String serverRedirect, SsoSetupDTO ssoSetupDTO) {
    return buildAuthorizationURI(state, serverRedirect);
  }

  public CompletionStage<SsoLoginResult<?>> oauth2callback(
      AbstractOAuth2Client<T, U, E> oauthClient,
      String state,
      String sessionState,
      String code,
      String serverBaseURL) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      log.warn("[AUTH]: OAuth2 state miss-match, session: {}, query: {}", sessionState, state);
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    String serverRedirectUri = serverBaseURL + callbackPath();
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
                  .socialLogin(email, fullName, location, getLoginMethod(), serverBaseURL)
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
