package com.rebrowse.auth.sso.oauth.google;

import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.sso.oauth.AbstractOAuthIdentityProvider;
import com.rebrowse.auth.sso.oauth.google.model.GoogleErrorResponse;
import com.rebrowse.auth.sso.oauth.google.model.GoogleScope;
import com.rebrowse.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.rebrowse.auth.sso.oauth.google.model.GoogleUserInfoResponse;
import com.rebrowse.auth.sso.setup.model.SsoMethod;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class GoogleIdentityProvider
    extends AbstractOAuthIdentityProvider<
        GoogleTokenResponse, GoogleUserInfoResponse, GoogleErrorResponse> {

  private static final String AUTHORIZATION_SERVER_URL =
      "https://accounts.google.com/o/oauth2/auth";

  private static final String SCOPES =
      String.join(
          " ",
          List.of(
              GoogleScope.OPENID.getValue(),
              GoogleScope.EMAIL.getValue(),
              GoogleScope.PROFILE.getValue()));

  @Inject GoogleOAuthClient client;

  @Override
  public SsoMethod getMethod() {
    return SsoMethod.GOOGLE;
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
  public CompletionStage<AuthorizationResponse> oauthCallback(
      String state, String sessionState, String code, URI serverBase) {
    return oauthCallback(client, state, sessionState, code, serverBase);
  }
}
