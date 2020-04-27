package com.meemaw.auth.sso.resource.v1.google;

import com.meemaw.auth.sso.service.google.SsoGoogleService;
import com.meemaw.shared.auth.SsoSession;
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

  @Inject
  SsoGoogleService ssoGoogleService;

  @Context
  UriInfo info;

  private String getRedirectUri() {
    return info.getBaseUri() + SsoGoogleResource.PATH + "/oauth2callback";
  }

  @Override
  public Response signin(String destination) {
    String state = ssoGoogleService.secureState(destination);
    URI Location = ssoGoogleService.buildAuthorizationURI(state, getRedirectUri());
    NewCookie sessionCookie = new NewCookie("state", state);
    return Response.status(Status.FOUND).cookie(sessionCookie).header("Location", Location).build();
  }

  @Override
  public CompletionStage<Response> oauth2callback(String state, String code, String sessionState) {
    return ssoGoogleService.oauth2callback(state, sessionState, code, getRedirectUri())
        .thenApply(ssoSocialLogin -> {
          String Location = ssoSocialLogin.getLocation();
          String SessionId = ssoSocialLogin.getSessionId();
          return Response.status(Status.FOUND).header("Location", Location)
              .cookie(SsoSession.cookie(SessionId)).build();
        });
  }


}
