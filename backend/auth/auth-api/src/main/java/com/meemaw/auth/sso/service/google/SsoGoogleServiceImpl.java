package com.meemaw.auth.sso.service.google;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.model.SsoSocialLogin;
import com.meemaw.auth.sso.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class SsoGoogleServiceImpl implements SsoGoogleService {

  public static final int SECURE_STATE_PREFIX_LENGTH = 26;
  private static final Collection<String> SCOPE_LIST = List.of("openid", "email", "profile");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);
  private static final String AUTHORIZATION_SERVER_URL =
      "https://accounts.google.com/o/oauth2/auth";

  private static final SecureRandom random = new SecureRandom();

  @Inject AppConfig appConfig;
  @Inject SsoGoogleClient ssoGoogleClient;
  @Inject SsoService ssoService;

  @Override
  public URI buildAuthorizationURI(String state, String redirectURI) {
    return UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
        .queryParam("client_id", appConfig.getGoogleOAuthClientId())
        .queryParam("redirect_uri", redirectURI)
        .queryParam("response_type", "code")
        .queryParam("scope", SCOPES)
        .queryParam("state", state)
        .build();
  }

  @Override
  public String secureState(String data) {
    String secureString =
        RandomStringUtils.random(SECURE_STATE_PREFIX_LENGTH, 0, 0, true, true, null, random);
    return secureString + data;
  }

  @Override
  @Traced
  @Timed(
      name = "oauth2callback",
      description = "A measure of how long it takes to do execute Google oauth2callback")
  public CompletionStage<SsoSocialLogin> oauth2callback(
      String state, String sessionState, String code, String redirectURI) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      log.warn("[AUTH]: State miss-match, session: {}, query: {}", sessionState, state);
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
                  URLDecoder.decode(
                      sessionState.substring(SECURE_STATE_PREFIX_LENGTH), StandardCharsets.UTF_8);
              String cookieDomain = RequestUtils.parseCookieDomain(location);
              MDC.put(LoggingConstants.USER_EMAIL, email);

              return ssoService
                  .socialLogin(email, fullName)
                  .thenApply(
                      loginResult -> {
                        log.info("[AUTH]: User authenticated via Google OAuth email: {}", email);
                        return new SsoSocialLogin(loginResult, location, cookieDomain);
                      });
            });
  }
}
