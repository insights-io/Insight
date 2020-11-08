package com.meemaw.auth.sso.oauth.github;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;

import com.meemaw.auth.sso.oauth.AbstractOAuthIdentityProvider;
import com.meemaw.auth.sso.oauth.github.model.GithubErrorResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubTokenResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubUserInfoResponse;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.SsoLoginResult;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

@ApplicationScoped
@Slf4j
public class GithubIdentityProvider
    extends AbstractOAuthIdentityProvider<
        GithubTokenResponse, GithubUserInfoResponse, GithubErrorResponse> {

  @Inject GithubOAuthClient client;

  private static final String AUTHORIZATION_SERVER_URL = "https://github.com/login/oauth/authorize";
  private static final Collection<String> SCOPE_LIST = List.of("read:user", "user:email");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);

  @Override
  public LoginMethod getLoginMethod() {
    return LoginMethod.GITHUB;
  }

  @Override
  public URI buildAuthorizationUri(String state, URI serverRedirectURI, @Nullable String email) {
    UriBuilder builder =
        UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
            .queryParam("client_id", appConfig.getGithubOpenIdClientId())
            .queryParam("redirect_uri", serverRedirectURI)
            .queryParam("response_type", "code")
            .queryParam("scope", SCOPES);

    if (email != null) {
      builder = builder.queryParam("login", email);
    }

    return builder.queryParam("state", state).build();
  }

  @Override
  @Traced
  @Timed(
      name = "oauth2callback",
      description = "A measure of how long it takes to do execute Github oauth2callback")
  public CompletionStage<SsoLoginResult<?>> oauthCallback(
      String state, String sessionState, String code, URI serverBaseURI) {
    return oauthCallback(client, state, sessionState, code, serverBaseURI);
  }
}
