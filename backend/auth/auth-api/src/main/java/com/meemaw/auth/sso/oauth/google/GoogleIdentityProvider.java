package com.meemaw.auth.sso.oauth.google;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;

import com.meemaw.auth.sso.oauth.AbstractOAuthIdentityProvider;
import com.meemaw.auth.sso.oauth.google.model.GoogleErrorResponse;
import com.meemaw.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.meemaw.auth.sso.oauth.google.model.GoogleUserInfoResponse;
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
public class GoogleIdentityProvider
    extends AbstractOAuthIdentityProvider<
        GoogleTokenResponse, GoogleUserInfoResponse, GoogleErrorResponse> {

  private static final Collection<String> SCOPE_LIST = List.of("openid", "email", "profile");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);
  private static final String AUTHORIZATION_SERVER_URL =
      "https://accounts.google.com/o/oauth2/auth";

  @Inject GoogleOAuthClient client;

  @Override
  public LoginMethod getLoginMethod() {
    return LoginMethod.GOOGLE;
  }

  @Override
  public URI buildAuthorizationUri(String state, URI redirect, @Nullable String email) {
    UriBuilder builder =
        UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
            .queryParam("client_id", appConfig.getGoogleOpenIdClientId())
            .queryParam("redirect_uri", redirect)
            .queryParam("response_type", "code")
            .queryParam("scope", SCOPES);

    if (email != null) {
      builder = builder.queryParam("login_hint", email);
    }

    return builder.queryParam("state", state).build();
  }

  @Override
  @Traced
  @Timed(
      name = "oauth2callback",
      description = "A measure of how long it takes to do execute Google oauth2callback")
  public CompletionStage<SsoLoginResult<?>> oauthCallback(
      String state, String sessionState, String code, URI serverBase) {
    return oauthCallback(client, state, sessionState, code, serverBase);
  }
}
