package com.meemaw.auth.sso.resource.v1;

import com.meemaw.auth.sso.model.LoginResult;
import com.meemaw.auth.sso.model.OAuthError;
import com.meemaw.auth.sso.model.OAuthUserInfo;
import com.meemaw.auth.sso.service.AbstractSsoOAuthService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSsoOAuthResource<T, U extends OAuthUserInfo, E extends OAuthError>
    implements SsoOAuthResource {

  @Context UriInfo info;
  @Context HttpServerRequest request;

  public Response signIn(AbstractSsoOAuthService<T, U, E> oauthService, String destinationPath) {
    String serverRedirectUri = getRedirectUri(info, request);
    String refererBaseURL =
        RequestUtils.parseRefererBaseURL(request)
            .orElseThrow(() -> Boom.badRequest().message("referer required").exception());
    String state = oauthService.secureState(refererBaseURL + destinationPath);
    URI location = oauthService.buildAuthorizationUri(state, serverRedirectUri);
    NewCookie sessionCookie = new NewCookie("state", state);

    log.info(
        "[AUTH]: OAuth sign in request redirect={} referer={} location={}",
        serverRedirectUri,
        refererBaseURL,
        location);

    return Response.status(Status.FOUND).cookie(sessionCookie).header("Location", location).build();
  }

  public CompletionStage<Response> oauth2callback(
      AbstractSsoOAuthService<T, U, E> oauthService,
      String state,
      String sessionState,
      String code) {
    String redirectUri = getRedirectUri(info, request);
    return oauthService
        .oauth2callback(state, sessionState, code, redirectUri)
        .thenApply(
            ssoSocialLogin -> {
              LoginResult<?> loginResult = ssoSocialLogin.getLoginResult();
              String location = ssoSocialLogin.getLocation();
              String cookieDomain = ssoSocialLogin.getCookieDomain();
              return loginResult.socialLoginResponse(location, cookieDomain);
            });
  }
}
