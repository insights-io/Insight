package com.meemaw.auth.sso.oauth.google;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.oauth.google.model.GoogleErrorResponse;
import com.meemaw.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.meemaw.auth.sso.oauth.google.model.GoogleUserInfoResponse;
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
public class OAuth2GoogleService
    extends AbstractOAuth2Service<
        GoogleTokenResponse, GoogleUserInfoResponse, GoogleErrorResponse> {

  private static final Collection<String> SCOPE_LIST = List.of("openid", "email", "profile");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);
  private static final String AUTHORIZATION_SERVER_URL =
      "https://accounts.google.com/o/oauth2/auth";

  @Inject AppConfig appConfig;
  @Inject OAuth2GoogleClient OAuth2GoogleClient;

  @Override
  public URI buildAuthorizationUri(String state, String redirectUri) {
    return UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
        .queryParam("client_id", appConfig.getGoogleOpenIdClientId())
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
      description = "A measure of how long it takes to do execute Google oauth2callback")
  public CompletionStage<SsoLoginResult<?>> oauth2callback(
      String state, String sessionState, String code, String redirectUri) {
    return oauth2callback(OAuth2GoogleClient, state, sessionState, code, redirectUri);
  }
}
