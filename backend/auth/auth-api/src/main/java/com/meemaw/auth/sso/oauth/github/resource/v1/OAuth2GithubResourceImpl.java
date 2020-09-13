package com.meemaw.auth.sso.oauth.github.resource.v1;

import com.meemaw.auth.sso.oauth.github.OAuth2GithubService;
import com.meemaw.auth.sso.oauth.github.model.GithubErrorResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubTokenResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Resource;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OAuth2GithubResourceImpl
    extends AbstractOAuth2Resource<GithubTokenResponse, GithubUserInfoResponse, GithubErrorResponse>
    implements OAuth2GithubResource {

  @Inject OAuth2GithubService oauthService;

  @Override
  public Response signIn(URL redirect) {
    return signIn(oauthService, redirect);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String code, String state, String sessionState) {
    return oauth2callback(oauthService, code, state, sessionState);
  }
}
