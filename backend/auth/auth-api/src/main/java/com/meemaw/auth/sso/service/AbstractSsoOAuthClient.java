package com.meemaw.auth.sso.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.model.OAuthError;
import com.meemaw.auth.sso.model.OAuthUserInfo;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

@Slf4j
public abstract class AbstractSsoOAuthClient<T, U extends OAuthUserInfo, E extends OAuthError> {

  @Inject Vertx vertx;

  protected WebClient webClient;

  @PostConstruct
  void initialize() {
    webClient = WebClient.create(vertx);
  }

  @Inject ObjectMapper objectMapper;

  public abstract Class<T> getTokenClazz();

  public abstract Class<U> getUserInfoClazz();

  public abstract Class<E> getErrorClazz();

  protected abstract CompletionStage<HttpResponse<Buffer>> requestUserInfo(T token);

  protected abstract CompletionStage<HttpResponse<Buffer>> requestCodeExchange(
      String code, String redirectUri);

  @Traced
  public CompletionStage<U> userInfo(T token) {
    return requestUserInfo(token)
        .thenApply(response -> parseResponse(response, getUserInfoClazz(), getErrorClazz()));
  }

  @Traced
  public CompletionStage<T> codeExchange(String code, String redirectUri) {
    return requestCodeExchange(code, redirectUri)
        .thenApply(response -> parseResponse(response, getTokenClazz(), getErrorClazz()));
  }

  protected <P> P parseResponse(
      HttpResponse<Buffer> response, Class<P> clazz, Class<E> errorClazz) {
    String jsonPayload = response.bodyAsString();
    int statusCode = response.statusCode();

    try {
      if (statusCode == Status.OK.getStatusCode()) {
        return objectMapper.readValue(jsonPayload, clazz);
      }

      E errorResponse = objectMapper.readValue(jsonPayload, errorClazz);
      throw Boom.status(statusCode).message(errorResponse.getMessage()).exception();
    } catch (JsonProcessingException ex) {
      log.error("[AUTH]: Failed to parse OAuth client response", ex);
      throw Boom.serverError().message(ex.getMessage()).exception(ex);
    }
  }
}
