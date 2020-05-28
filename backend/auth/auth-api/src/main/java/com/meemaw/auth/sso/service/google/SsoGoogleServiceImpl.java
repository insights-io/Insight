package com.meemaw.auth.sso.service.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.model.SsoSocialLogin;
import com.meemaw.auth.sso.model.google.GoogleErrorResponse;
import com.meemaw.auth.sso.model.google.GoogleTokenResponse;
import com.meemaw.auth.sso.model.google.GoogleUserInfoResponse;
import com.meemaw.auth.sso.service.SsoService;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.axle.core.Vertx;
import io.vertx.axle.core.buffer.Buffer;
import io.vertx.axle.ext.web.client.HttpResponse;
import io.vertx.axle.ext.web.client.WebClient;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
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

  private static final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";
  private static final String TOKEN_INFO_SERVER_URL = "https://oauth2.googleapis.com/tokeninfo";

  @ConfigProperty(name = "google.oauth.client.id")
  String googleOAuthClientId;

  @ConfigProperty(name = "google.oauth.client.secret")
  String googleOAuthClientSecret;

  @Inject ObjectMapper objectMapper;

  @Inject SsoService ssoService;

  @Inject Vertx vertx;

  private WebClient webClient;

  @PostConstruct
  void initialize() {
    webClient = WebClient.create(vertx);
  }

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

  /**
   * Generates a secure state with a secure random string of length 26 as a prefix.
   *
   * @param destination to redirect to after a successful authorization
   * @return secure state
   */
  @Override
  public String secureState(String destination) {
    String secureString = new BigInteger(130, new SecureRandom()).toString(32);
    return secureString + destination;
  }

  @Override
  public CompletionStage<SsoSocialLogin> oauth2callback(
      String state, String sessionState, String code, String redirectURI) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(state)) {
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    return exchangeCode(code, redirectURI)
        .thenCompose(this::userInfo)
        .thenCompose(
            userInfo -> {
              String destination = sessionState.substring(26);
              String email = userInfo.getEmail();
              String location = URLDecoder.decode(destination, StandardCharsets.UTF_8);

              log.info("Google oauth2callback redirecting {} to {}", email, location);
              return ssoService
                  .socialLogin(email)
                  .thenApply(sessionId -> new SsoSocialLogin(sessionId, location));
            });
  }

  /**
   * Exchange authorization code for the access token and ID token.
   *
   * @param code google authorization code
   * @param redirectURI server oauth2callback redirect URL
   * @return GoogleTokenResponse
   */
  private CompletionStage<GoogleTokenResponse> exchangeCode(String code, String redirectURI) {
    return webClient
        .postAbs(TOKEN_SERVER_URL)
        .addQueryParam("grant_type", "authorization_code")
        .addQueryParam("code", code)
        .addQueryParam("client_id", googleOAuthClientId)
        .addQueryParam("client_secret", googleOAuthClientSecret)
        .addQueryParam("redirect_uri", redirectURI)
        .putHeader("Content-Length", "0")
        .send()
        .thenApply(this::parseTokenResponse);
  }

  private GoogleTokenResponse parseTokenResponse(HttpResponse<Buffer> response) {
    return handleGoogleResponse(response, GoogleTokenResponse.class);
  }

  /**
   * Validation of ID token;
   * https://developers.google.com/identity/sign-in/web/backend-auth#calling-the-tokeninfo-endpoint
   *
   * @param token from google response
   * @return google user info
   */
  private CompletionStage<GoogleUserInfoResponse> userInfo(GoogleTokenResponse token) {
    return webClient
        .getAbs(TOKEN_INFO_SERVER_URL)
        .addQueryParam("id_token", token.getIdToken())
        .send()
        .thenApply(response -> handleGoogleResponse(response, GoogleUserInfoResponse.class));
  }

  private <T> T handleGoogleResponse(HttpResponse<Buffer> response, Class<T> clazz) {
    String jsonPayload = response.bodyAsString();
    int statusCode = response.statusCode();

    try {
      if (statusCode == Status.OK.getStatusCode()) {
        return objectMapper.readValue(jsonPayload, clazz);
      }

      GoogleErrorResponse errorResponse =
          objectMapper.readValue(jsonPayload, GoogleErrorResponse.class);
      String errorDescription = errorResponse.getErrorDescription();
      String error = errorResponse.getError();
      String message = String.format("%s. %s", error, errorDescription);
      throw Boom.status(statusCode).message(message).exception();
    } catch (JsonProcessingException ex) {
      log.error("Failed to parse google access token claims", ex);
      throw Boom.serverError().message(ex.getMessage()).exception(ex);
    }
  }
}
