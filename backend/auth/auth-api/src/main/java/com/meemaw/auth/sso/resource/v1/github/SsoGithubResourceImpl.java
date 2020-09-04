package com.meemaw.auth.sso.resource.v1.github;

import com.meemaw.auth.sso.resource.v1.AbstractSsoOAuthResource;
import com.meemaw.auth.sso.service.github.SsoGithubService;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class SsoGithubResourceImpl extends AbstractSsoOAuthResource implements SsoGithubResource {

  @Inject SsoGithubService oauthService;

  @Override
  public Response signIn(String destination) {
    return signIn(oauthService, destination);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String state, String code, String sessionState) {
    return oauth2callback(oauthService, state, sessionState, code);
  }
}
