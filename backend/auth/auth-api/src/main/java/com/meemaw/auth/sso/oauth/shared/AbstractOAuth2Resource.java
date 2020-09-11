package com.meemaw.auth.sso.oauth.shared;

import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.oauth.model.OAuthError;
import com.meemaw.auth.sso.oauth.model.OAuthUserInfo;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOAuth2Resource<T, U extends OAuthUserInfo, E extends OAuthError>
    implements OAuth2Resource {

  @Context UriInfo info;
  @Context HttpServerRequest request;

  public abstract String getBasePath();

  public String getRedirectUri(UriInfo info, HttpServerRequest request) {
    return RequestUtils.getServerBaseURL(info, request) + getBasePath() + "/" + CALLBACK_PATH;
  }

  public Response signIn(AbstractOAuth2Service<T, U, E> oauthService, String refererRedirect) {
    String serverRedirectUri = getRedirectUri(info, request);
    String refererBaseURL =
        RequestUtils.parseRefererBaseURL(request)
            .orElseThrow(() -> Boom.badRequest().message("referer required").exception());
    String state = oauthService.secureState(refererBaseURL + refererRedirect);
    URI location = oauthService.buildAuthorizationUri(state, serverRedirectUri);

    log.info(
        "[AUTH]: OAuth2 sign in request redirect={} referer={} location={}",
        serverRedirectUri,
        refererBaseURL,
        location);

    return Response.status(Status.FOUND)
        .cookie(SsoSignInSession.cookie(state))
        .header("Location", location)
        .build();
  }

  public CompletionStage<Response> oauth2callback(
      AbstractOAuth2Service<T, U, E> oauthService, String code, String state, String sessionState) {
    String redirectUri = getRedirectUri(info, request);
    return oauthService
        .oauth2callback(state, sessionState, code, redirectUri)
        .thenApply(SsoLoginResult::response);
  }
}
