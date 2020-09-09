package com.meemaw.auth.sso.oauth.github;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.oauth.github.model.GithubErrorResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubTokenResponse;
import com.meemaw.auth.sso.oauth.github.model.GithubUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Service;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class OAuth2GithubService
    extends AbstractOAuth2Service<
        GithubTokenResponse, GithubUserInfoResponse, GithubErrorResponse> {

  @Inject AppConfig appConfig;
  @Inject OAuth2GithubClient OAuth2GithubClient;

  private static final String AUTHORIZATION_SERVER_URL = "https://github.com/login/oauth/authorize";
  private static final Collection<String> SCOPE_LIST = List.of("read:user", "user:email");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);

  @Override
  public URI buildAuthorizationUri(String state, String redirectUri) {
    return UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
        .queryParam("client_id", appConfig.getGithubOpenIdClientId())
        .queryParam("redirect_uri", redirectUri)
        .queryParam("response_type", "code")
        .queryParam("scope", SCOPES)
        .queryParam("state", state)
        .build();
  }

  @Override
  @Traced
  @Timed(
      name = "oauth2callback",
      description = "A measure of how long it takes to do execute Github oauth2callback")
  public CompletionStage<SsoLoginResult<?>> oauth2callback(
      String state, String sessionState, String code, String redirectUri) {
    return oauth2callback(OAuth2GithubClient, state, sessionState, code, redirectUri);
  }
}
