package com.meemaw.auth.sso.oauth.microsoft;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftErrorResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Service;
import com.meemaw.auth.sso.session.model.LoginMethod;
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
public class OAuth2MicrosoftService
    extends AbstractOAuth2Service<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse> {

  private static final Collection<String> SCOPE_LIST = List.of("openid", "email", "profile");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);
  private static final String AUTHORIZATION_SERVER_URL =
      "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";

  @Inject AppConfig appConfig;
  @Inject OAuth2MicrosoftClient OAuth2MicrosoftClient;

  @Override
  public LoginMethod getLoginMethod() {
    return LoginMethod.MICROSOFT;
  }

  @Override
  public URI buildAuthorizationURL(String state, URI serverRedirect) {
    return UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
        .queryParam("client_id", appConfig.getMicrosoftOpenIdClientId())
        .queryParam("redirect_uri", serverRedirect)
        .queryParam("response_type", "code")
        .queryParam("scope", SCOPES)
        .queryParam("response_mode", "query")
        .queryParam("state", state)
        .build();
  }

  @Override
  @Traced
  @Timed(
      name = "oauth2callback",
      description = "A measure of how long it takes to do execute Microsoft oauth2callback")
  public CompletionStage<SsoLoginResult<?>> oauth2callback(
      String state, String sessionState, String code, URI serverBaseURI) {
    log.info("[AUTH]: OAuth2 callback request code={} serverBaseURI={}", code, serverBaseURI);
    return oauth2callback(OAuth2MicrosoftClient, state, sessionState, code, serverBaseURI);
  }
}
