package com.meemaw.auth.sso.oauth.google.resource.v1;

import com.meemaw.auth.sso.oauth.google.OAuth2GoogleService;
import com.meemaw.auth.sso.oauth.google.model.GoogleErrorResponse;
import com.meemaw.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.meemaw.auth.sso.oauth.google.model.GoogleUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Resource;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OAuth2GoogleResourceImpl
    extends AbstractOAuth2Resource<GoogleTokenResponse, GoogleUserInfoResponse, GoogleErrorResponse>
    implements OAuth2GoogleResource {

  @Inject OAuth2GoogleService oauthService;

  @Override
  public CompletionStage<Response> signIn(URL redirect, @Nullable String email) {
    return signIn(oauthService, redirect, email);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String code, String state, String sessionState) {
    return oauth2callback(oauthService, code, state, sessionState);
  }
}
