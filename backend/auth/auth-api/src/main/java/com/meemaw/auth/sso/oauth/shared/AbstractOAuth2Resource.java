package com.meemaw.auth.sso.oauth.shared;

import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.oauth.OAuth2Resource;
import com.meemaw.auth.sso.oauth.model.OAuthError;
import com.meemaw.auth.sso.oauth.model.OAuthUserInfo;
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
public abstract class AbstractOAuth2Resource<T, U extends OAuthUserInfo, E extends OAuthError>
    implements OAuth2Resource {

  @Context UriInfo info;
  @Context HttpServerRequest request;

  public URI getServerRedirectURI(
      AbstractOAuth2Service<T, U, E> oauthService, UriInfo info, HttpServerRequest request) {
    return UriBuilder.fromUri(RequestUtils.getServerBaseURI(info, request))
        .path(oauthService.callbackPath())
        .build();
  }

  public CompletionStage<Response> signIn(
      AbstractOAuth2Service<T, U, E> oauthService, URL redirect, @Nullable String email) {
    String state = oauthService.secureState(redirect.toString());
    URI serverRedirectURI = getServerRedirectURI(oauthService, info, request);
    URI authorizationURI = oauthService.buildAuthorizationURL(state, serverRedirectURI, email);
    String cookieDomain = RequestUtils.parseCookieDomain(serverRedirectURI);
    log.info("[AUTH]: OAuth2 sign in request authorizationURI={}", authorizationURI);

    return CompletableFuture.completedStage(
        Response.status(Status.FOUND)
            .cookie(SsoSignInSession.cookie(state, cookieDomain))
            .header("Location", authorizationURI)
            .build());
  }

  public CompletionStage<Response> oauth2callback(
      AbstractOAuth2Service<T, U, E> oauthService, String code, String state, String sessionState) {
    URI serverBase = RequestUtils.getServerBaseURI(info, request);
    return oauthService
        .oauth2callback(state, sessionState, code, serverBase)
        .thenApply(SsoLoginResult::response);
  }
}
