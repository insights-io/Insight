package com.meemaw.auth.sso.oauth.microsoft.resource.v1;

import com.meemaw.auth.sso.oauth.AbstractOAuthResource;
import com.meemaw.auth.sso.oauth.microsoft.MicrosoftIdentityProvider;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftErrorResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;

import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class MicrosoftOAuthResourceImpl
    extends AbstractOAuthResource<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse>
    implements MicrosoftOAuthResource {

  @Inject MicrosoftIdentityProvider identityProvider;

  @Override
  public CompletionStage<Response> signIn(URL redirect, @Nullable String email) {
    return signIn(identityProvider, redirect, email);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String code, String state, String sessionState) {
    return oauthCallback(identityProvider, code, state, sessionState);
  }
}
