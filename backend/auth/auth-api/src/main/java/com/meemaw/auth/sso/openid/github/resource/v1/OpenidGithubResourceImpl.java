package com.meemaw.auth.sso.openid.github.resource.v1;

import com.meemaw.auth.sso.openid.github.OpenIdGithubService;
import com.meemaw.auth.sso.openid.github.model.GithubErrorResponse;
import com.meemaw.auth.sso.openid.github.model.GithubTokenResponse;
import com.meemaw.auth.sso.openid.github.model.GithubUserInfoResponse;
import com.meemaw.auth.sso.openid.shared.AbstractOpenIdResource;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OpenidGithubResourceImpl
    extends AbstractOpenIdResource<GithubTokenResponse, GithubUserInfoResponse, GithubErrorResponse>
    implements OpenidGithubResource {

  @Inject OpenIdGithubService oauthService;

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
    return PATH;
  }
}
