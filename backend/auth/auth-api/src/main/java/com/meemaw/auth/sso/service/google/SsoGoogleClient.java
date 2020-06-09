package com.meemaw.auth.sso.service.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.model.google.GoogleErrorResponse;
import com.meemaw.auth.sso.model.google.GoogleTokenResponse;
import com.meemaw.auth.sso.model.google.GoogleUserInfoResponse;
import com.meemaw.shared.rest.response.Boom;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class SsoGoogleClient {

  private static final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";
  private static final String TOKEN_INFO_SERVER_URL = "https://oauth2.googleapis.com/tokeninfo";

  @ConfigProperty(name = "google.oauth.client.id")
  String googleOAuthClientId;

  @ConfigProperty(name = "google.oauth.client.secret")
  String googleOAuthClientSecret;

  @Inject ObjectMapper objectMapper;
  @Inject Vertx vertx;

  private WebClient webClient;

  @PostConstruct
  void initialize() {
    webClient = WebClient.create(vertx);
  }

  /**
   * Exchange authorization code for the access token and ID token.
   *
   * @param code String google authorization code
   * @param redirectURI String server oauth2callback redirect URL
   * @return GoogleTokenResponse
   */
  public CompletionStage<GoogleTokenResponse> codeExchange(String code, String redirectURI) {
    return requestCodeExchange(code, redirectURI)
        .map(response -> parseGoogleResponse(response, GoogleTokenResponse.class))
        .subscribeAsCompletionStage();
  }

  private Uni<HttpResponse<Buffer>> requestCodeExchange(String code, String redirectURI) {
    return webClient
        .postAbs(TOKEN_SERVER_URL)
        .addQueryParam("grant_type", "authorization_code")
        .addQueryParam("code", code)
        .addQueryParam("client_id", googleOAuthClientId)
        .addQueryParam("client_secret", googleOAuthClientSecret)
        .addQueryParam("redirect_uri", redirectURI)
        .putHeader("Content-Length", "0")
        .send();
  }

  /**
   * Validation of ID token;
   * https://developers.google.com/identity/sign-in/web/backend-auth#calling-the-tokeninfo-endpoint
   *
   * @param token GoogleTokenResponse
   * @return GoogleUserInfoResponse google user info response
   */
  public CompletionStage<GoogleUserInfoResponse> userInfo(GoogleTokenResponse token) {
    return requestUserInfo(token)
        .map(response -> parseGoogleResponse(response, GoogleUserInfoResponse.class))
        .subscribeAsCompletionStage();
  }

  private Uni<HttpResponse<Buffer>> requestUserInfo(GoogleTokenResponse token) {
    return webClient
        .getAbs(TOKEN_INFO_SERVER_URL)
        .addQueryParam("id_token", token.getIdToken())
        .send();
  }

  private <T> T parseGoogleResponse(HttpResponse<Buffer> response, Class<T> clazz) {
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
