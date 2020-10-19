package com.meemaw.auth.sso.oauth.shared;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.AbstractIdentityProvider;
import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.oauth.model.OAuthError;
import com.meemaw.auth.sso.oauth.model.OAuthUserInfo;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractOAuthIdentityProvider<
        T, U extends OAuthUserInfo, E extends OAuthError>
    extends AbstractIdentityProvider {

  @Inject protected AppConfig appConfig;
  @Inject SsoService ssoService;

  public abstract URI buildAuthorizationUri(
      String state, URI serverRedirectUri, @Nullable String email);

  public abstract CompletionStage<SsoLoginResult<?>> oauthCallback(
      String state, String sessionState, String code, URI serverBase);

  @Override
  public String basePath() {
    return String.join("/", OAuthResource.PATH, getLoginMethod().getKey());
  }

  public CompletionStage<SsoLoginResult<?>> oauthCallback(
      AbstractOAuthClient<T, U, E> oauthClient,
      String state,
      String sessionState,
      String code,
      URI serverBase) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      log.warn("[AUTH]: OAuth state miss-match, session: {}, query: {}", sessionState, state);
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    URL redirect = RequestUtils.sneakyURL(secureStateData(sessionState));
    URI serverRedirect = UriBuilder.fromUri(serverBase).path(callbackPath()).build();

    return oauthClient
        .codeExchange(code, serverRedirect)
        .thenCompose(oauthClient::userInfo)
        .thenCompose(
            userInfo -> {
              String fullName = userInfo.getFullName();
              String email = userInfo.getEmail();
              String cookieDomain = RequestUtils.parseCookieDomain(redirect);
              MDC.put(LoggingConstants.USER_EMAIL, email);
              log.info("[AUTH]: OAuth successfully retrieved user info email={}", email);

              return ssoService
                  .socialLogin(email, fullName, getLoginMethod(), redirect, serverBase)
                  .exceptionally(throwable -> handleSsoException(throwable, redirect))
                  .thenApply(
                      loginResult -> {
                        log.info(
                            "[AUTH]: OAuth flow successful email={} redirect={}", email, redirect);
                        return new SsoLoginResult<>(loginResult, cookieDomain);
                      });
            });
  }
}
