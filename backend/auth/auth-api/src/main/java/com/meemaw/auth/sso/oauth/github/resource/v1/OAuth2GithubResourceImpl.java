package com.meemaw.auth.sso.oauth.github.resource.v1;

import com.meemaw.auth.sso.oauth.github.OAuth2GithubService;
import com.meemaw.auth.sso.oauth.github.model.GithubErrorResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubTokenResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Resource;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OAuth2GithubResourceImpl
    extends AbstractOAuth2Resource<GithubTokenResponse, GithubUserInfoResponse, GithubErrorResponse>
    implements OAuth2GithubResource {

  @Inject OAuth2GithubService oauthService;

  @Override
  public Response signIn(String destination) {
    return signIn(oauthService, destination);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String state, String code, String sessionState) {
    return oauth2callback(oauthService, state, sessionState, code);
  }

  @Override
  public String getBasePath() {
    return OAuth2GithubResource.PATH;
  }
}
