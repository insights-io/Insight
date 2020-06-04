package com.meemaw.auth.sso.resource.v1.google;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.service.google.SsoGoogleService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoGoogleResourceImpl implements SsoGoogleResource {

  private static final String GOOGLE_OAUTH2_CALLBACK_PATH =
      SsoGoogleResource.PATH + "/" + SsoGoogleResource.OAUTH2_CALLBACK_PATH;

  @Inject SsoGoogleService ssoGoogleService;
  @Context UriInfo info;
  @Context HttpServerRequest request;

  private String getRedirectUri() {
    return RequestUtils.getServerBaseURL(info, request) + GOOGLE_OAUTH2_CALLBACK_PATH;
  }

  @Override
  public Response signIn(String destinationPath) {
    String serverRedirectURI = getRedirectUri();
    String refererBaseURL =
        RequestUtils.parseRefererBaseURL(request)
            .orElseThrow(() -> Boom.badRequest().message("referer required").exception());

    String state = ssoGoogleService.secureState(refererBaseURL + destinationPath);
    URI location = ssoGoogleService.buildAuthorizationURI(state, serverRedirectURI);
    NewCookie sessionCookie = new NewCookie("state", state);
    return Response.status(Status.FOUND).cookie(sessionCookie).header("Location", location).build();
  }

  @Override
  public CompletionStage<Response> oauth2callback(String state, String code, String sessionState) {
    return ssoGoogleService
        .oauth2callback(state, sessionState, code, getRedirectUri())
        .thenApply(
            ssoSocialLogin -> {
              String location = ssoSocialLogin.getLocation();
              String sessionId = ssoSocialLogin.getSessionId();
              String cookieDomain = ssoSocialLogin.getCookieDomain();

              return Response.status(Status.FOUND)
                  .header("Location", location)
                  .cookie(SsoSession.cookie(sessionId, cookieDomain))
                  .build();
            });
  }
}
