package com.meemaw.auth.sso.service.google;

import com.meemaw.auth.sso.model.SsoSocialLogin;
import com.meemaw.auth.sso.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class SsoGoogleServiceImpl implements SsoGoogleService {

  private static final Collection<String> SCOPE_LIST = List.of("openid", "email", "profile");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);
  private static final String AUTHORIZATION_SERVER_URL =
      "https://accounts.google.com/o/oauth2/auth";

  @ConfigProperty(name = "google.oauth.client.id")
  String googleOAuthClientId;

  @ConfigProperty(name = "google.oauth.client.secret")
  String googleOAuthClientSecret;

  @Inject SsoGoogleClient ssoGoogleClient;
  @Inject SsoService ssoService;

  @Override
  public URI buildAuthorizationURI(String state, String redirectURI) {
    return UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
        .queryParam("client_id", googleOAuthClientId)
        .queryParam("redirect_uri", redirectURI)
        .queryParam("response_type", "code")
        .queryParam("scope", SCOPES)
        .queryParam("state", state)
        .build();
  }

  @Override
  public String secureState(String data) {
    String secureString = new BigInteger(130, new SecureRandom()).toString(32);
    return secureString + data;
  }

  @Override
  public CompletionStage<SsoSocialLogin> oauth2callback(
      String state, String sessionState, String code, String redirectURI) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      log.debug("State miss-match, session: {}, query: {}", sessionState, state);
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    return ssoGoogleClient
        .codeExchange(code, redirectURI)
        .thenCompose(ssoGoogleClient::userInfo)
        .thenCompose(
            userInfo -> {
              String fullName = String.join(" ", userInfo.getGivenName(), userInfo.getFamilyName());
              String email = userInfo.getEmail();
              String location =
                  URLDecoder.decode(sessionState.substring(26), StandardCharsets.UTF_8);
              String cookieDomain = RequestUtils.parseCookieDomain(location);

              return ssoService
                  .socialLogin(email, fullName)
                  .thenApply(sessionId -> new SsoSocialLogin(sessionId, location, cookieDomain));
            });
  }
}
