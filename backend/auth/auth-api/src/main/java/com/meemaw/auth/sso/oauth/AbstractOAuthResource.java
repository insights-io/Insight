package com.meemaw.auth.sso.oauth;

import com.meemaw.auth.sso.AbstractIdentityProvider;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.shared.context.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOAuthResource<T, U extends OAuthUserInfo, E extends OAuthError>
    implements OAuthResource {

  @Context UriInfo info;
  @Context HttpServerRequest request;

  public URI getServerRedirectUri(
      AbstractOAuthIdentityProvider<T, U, E> oauthService,
      UriInfo info,
      HttpServerRequest request) {
    return UriBuilder.fromUri(RequestUtils.getServerBaseUri(info, request))
        .path(oauthService.callbackPath())
        .build();
  }

  public CompletionStage<Response> signIn(
      AbstractOAuthIdentityProvider<T, U, E> oauthService, URL redirect, @Nullable String email) {
    String state = AbstractIdentityProvider.secureState(redirect.toString());
    URI serverRedirectUri = getServerRedirectUri(oauthService, info, request);
    URI authorizationUri = oauthService.buildAuthorizationUri(state, serverRedirectUri, email);
    String cookieDomain = RequestUtils.parseCookieDomain(serverRedirectUri);
    log.debug("[AUTH]: OAuth2 sign in request authorizationUri={}", authorizationUri);

    return CompletableFuture.completedStage(
        Response.status(Status.FOUND)
            .cookie(SsoSignInSession.cookie(state, cookieDomain))
            .location(authorizationUri)
            .build());
  }

  public CompletionStage<Response> oauthCallback(
      AbstractOAuthIdentityProvider<T, U, E> identityProvider,
      String code,
      String state,
      String sessionState) {
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    return identityProvider
        .oauthCallback(state, sessionState, code, serverBaseUri)
        .thenApply(SsoLoginResult::response);
  }
}
