package com.meemaw.auth.sso.openid.microsoft;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.model.SsoSocialLogin;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftErrorResponse;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.openid.shared.AbstractOpenIdService;
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
public class OpenIdMicrosoftService
    extends AbstractOpenIdService<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse> {

  private static final Collection<String> SCOPE_LIST = List.of("openid", "email", "profile");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);
  private static final String AUTHORIZATION_SERVER_URL =
      "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";

  @Inject AppConfig appConfig;
  @Inject OpenIdMicrosoftClient openIdMicrosoftClient;

  @Override
  public URI buildAuthorizationUri(String state, String redirectUri) {
    return UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
        .queryParam("client_id", appConfig.getMicrosoftOpenIdClientId())
        .queryParam("redirect_uri", redirectUri)
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
  public CompletionStage<SsoSocialLogin> oauth2callback(
      String state, String sessionState, String code, String redirectUri) {
    log.info("[AUTH]: OpenID oauth2callback request code={} redirectUri={}", code, redirectUri);
    return oauth2callback(openIdMicrosoftClient, state, sessionState, code, redirectUri);
  }
}
