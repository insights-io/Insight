package com.rebrowse.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.auth.core.config.model.AppConfig;
import com.rebrowse.auth.sso.oauth.OAuthResource;
import com.rebrowse.auth.sso.oauth.github.resource.v1.GithubOAuthResource;
import com.rebrowse.auth.sso.oauth.google.resource.v1.GoogleOAuthResource;
import com.rebrowse.auth.sso.oauth.microsoft.resource.v1.MicrosoftOAuthResource;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

public class AuthorizationSsoFlows extends AbstractTestFlow {

  private final AppConfig appConfig;

  private final URI googleOAuthCallback;
  private final URI githubOAuthCallback;
  private final URI microsoftOAuthCallback;

  public AuthorizationSsoFlows(URI baseUri, ObjectMapper objectMapper, AppConfig appConfig) {
    super(baseUri, objectMapper);
    this.appConfig = appConfig;

    this.googleOAuthCallback =
        UriBuilder.fromUri(baseUri)
            .path(GoogleOAuthResource.PATH)
            .path(OAuthResource.CALLBACK_PATH)
            .build();
    this.githubOAuthCallback =
        UriBuilder.fromUri(baseUri)
            .path(GithubOAuthResource.PATH)
            .path(OAuthResource.CALLBACK_PATH)
            .build();
    this.microsoftOAuthCallback =
        UriBuilder.fromUri(baseUri)
            .path(MicrosoftOAuthResource.PATH)
            .path(OAuthResource.CALLBACK_PATH)
            .build();
  }

  public String microsoftAuthorizationPattern(String email) {
    return AuthApiTestUtils.microsoftOAuthAuthorizePattern(
        appConfig.getMicrosoftOpenIdClientId(), microsoftOAuthCallback, email);
  }

  public String githubAuthorizationPattern(String email) {
    return AuthApiTestUtils.githubOAuthAuthorizePattern(
        appConfig.getGithubOpenIdClientId(), githubOAuthCallback, email);
  }

  public String googleAuthorizationPattern(String email) {
    return AuthApiTestUtils.googleOAuthAuthorizePattern(
        appConfig.getGoogleOpenIdClientId(), googleOAuthCallback, email);
  }
}
