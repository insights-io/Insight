package com.meemaw.auth.sso.openid.google.resource.v1;

import com.meemaw.auth.sso.openid.google.OpenIdGoogleService;
import com.meemaw.auth.sso.openid.google.model.GoogleErrorResponse;
import com.meemaw.auth.sso.openid.google.model.GoogleTokenResponse;
import com.meemaw.auth.sso.openid.google.model.GoogleUserInfoResponse;
import com.meemaw.auth.sso.openid.shared.AbstractOpenIdResource;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OpenIdGoogleResourceImpl
    extends AbstractOpenIdResource<GoogleTokenResponse, GoogleUserInfoResponse, GoogleErrorResponse>
    implements OpenIdGoogleResource {

  @Inject OpenIdGoogleService oauthService;

  @Override
  public Response signIn(String destinationPath) {
    return signIn(oauthService, destinationPath);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String state, String code, String sessionState) {
    return oauth2callback(oauthService, state, sessionState, code);
  }

  @Override
  public String getBasePath() {
    return PATH;
  }
}
