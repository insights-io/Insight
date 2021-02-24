package com.rebrowse.auth.sso.oauth.github.resource.v1;

import com.rebrowse.auth.sso.oauth.AbstractOAuthResource;
import com.rebrowse.auth.sso.oauth.github.GithubIdentityProvider;
import com.rebrowse.auth.sso.oauth.github.model.GithubErrorResponse;
import com.rebrowse.auth.sso.oauth.github.model.GithubTokenResponse;
import com.rebrowse.auth.sso.oauth.github.model.GithubUserInfoResponse;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class GithubOAuthResourceImpl
    extends AbstractOAuthResource<GithubTokenResponse, GithubUserInfoResponse, GithubErrorResponse>
    implements GithubOAuthResource {

  @Inject GithubIdentityProvider identityProvider;

  @Override
  public CompletionStage<Response> signIn(URL redirect, @Nullable String email) {
    return signIn(identityProvider, redirect, email);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String code, String state, String sessionState) {
    return oauthCallback(identityProvider, code, state, sessionState);
  }
}
