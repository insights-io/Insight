package com.rebrowse.auth.sso.oauth;

import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.request.SsoAuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.core.config.model.AppConfig;
import com.rebrowse.auth.sso.AbstractIdentityProvider;
import com.rebrowse.auth.sso.session.service.SsoService;
import com.rebrowse.auth.sso.setup.model.dto.SsoSetupDTO;
import com.rebrowse.shared.context.URIUtils;
import com.rebrowse.shared.logging.LoggingConstants;
import com.rebrowse.shared.rest.response.Boom;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
  @Inject protected SsoService ssoService;

  @Override
  public String getResourcePath() {
    return String.join("/", OAuthResource.PATH, getMethod().getKey());
  }

  public abstract CompletionStage<AuthorizationResponse> oauthCallback(
      String state, String sessionState, String code, URI serverBaseUri);

  public CompletionStage<AuthorizationResponse> oauthCallback(
      AbstractOAuthClient<T, U, E> client,
      String state,
      String sessionState,
      String code,
      URI serverBaseUri) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      log.debug("[AUTH]: OAuth state miss-match, session={}, query={}", sessionState, state);
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    URI clientRedirect = URI.create(secureStateData(sessionState));
    URI serverRedirect = UriBuilder.fromUri(serverBaseUri).path(getCallbackPath()).build();

    return client
        .codeExchange(code, serverRedirect)
        .thenCompose(client::userInfo)
        .thenCompose(
            userInfo -> {
              String fullName = userInfo.getFullName();
              String email = userInfo.getEmail();
              String cookieDomain = URIUtils.parseCookieDomain(clientRedirect);
              MDC.put(LoggingConstants.USER_EMAIL, email);
              log.info("[AUTH]: OAuth successfully retrieved user info email={}", email);

              return ssoService
                  .authorizeSocialSso(email, fullName, getMethod(), clientRedirect, serverBaseUri)
                  .exceptionally(
                      throwable ->
                          ssoErrorAuthorizationResponse(throwable, cookieDomain, clientRedirect));
            });
  }

  @Override
  public CompletionStage<SsoAuthorizationRequest> getSsoAuthorizationRequest(
      SsoSetupDTO ssoSetup, AuthorizationRequest authorizationRequest) {
    String state = secureState(authorizationRequest.getRedirect().toString());
    URI serverCallback = getCallbackEndpoint(authorizationRequest.getServerBaseUri());
    URI location = buildAuthorizationUri(state, serverCallback, authorizationRequest.getEmail());
    return CompletableFuture.completedStage(
        new SsoAuthorizationRequest(location, authorizationRequest.getDomain(), state));
  }
}
