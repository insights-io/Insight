package com.rebrowse.auth.sso.oauth.google.resource.v1;

import com.rebrowse.auth.sso.oauth.AbstractOAuthResource;
import com.rebrowse.auth.sso.oauth.google.GoogleIdentityProvider;
import com.rebrowse.auth.sso.oauth.google.model.GoogleErrorResponse;
import com.rebrowse.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.rebrowse.auth.sso.oauth.google.model.GoogleUserInfoResponse;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class GoogleOAuthResourceImpl
    extends AbstractOAuthResource<GoogleTokenResponse, GoogleUserInfoResponse, GoogleErrorResponse>
    implements GoogleOAuthResource {

  @Inject GoogleIdentityProvider identityProvider;

  @Override
  public CompletionStage<Response> signIn(URL redirect, @Nullable String email) {
    return signIn(identityProvider, redirect, email);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String code, String state, String sessionState) {
    return oauthCallback(identityProvider, code, state, sessionState);
  }
}
