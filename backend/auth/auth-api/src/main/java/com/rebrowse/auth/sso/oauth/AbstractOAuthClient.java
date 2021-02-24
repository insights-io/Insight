package com.rebrowse.auth.sso.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.shared.rest.response.Boom;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

@Slf4j
public abstract class AbstractOAuthClient<T, U extends OAuthUserInfo, E extends OAuthError> {

  protected WebClient webClient;
  @Inject Vertx vertx;
  @Inject ObjectMapper objectMapper;

  @PostConstruct
  void initialize() {
    webClient = WebClient.create(vertx);
  }

  public abstract Class<T> getTokenClazz();

  public abstract Class<U> getUserInfoClazz();

  public abstract Class<E> getErrorClazz();

  protected abstract CompletionStage<HttpResponse<Buffer>> requestUserInfo(T token);

  protected abstract CompletionStage<HttpResponse<Buffer>> requestCodeExchange(
      String code, URI serverRedirect);

  @Traced
  public CompletionStage<U> userInfo(T token) {
    log.info("[AUTH]: OAuth userInfo request token={}", token);
    return requestUserInfo(token)
        .thenApply(response -> parseResponse(response, getUserInfoClazz(), getErrorClazz()));
  }

  @Traced
  public CompletionStage<T> codeExchange(String code, URI redirect) {
    log.info("[AUTH]: OAuth code exchange request code={} redirect={}", code, redirect);
    return requestCodeExchange(code, redirect)
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

      log.error("[AUTH]: OAuth request failed status={} response={}", statusCode, jsonPayload);
      E errorResponse = objectMapper.readValue(jsonPayload, errorClazz);
      throw Boom.status(statusCode).message(errorResponse.getMessage()).exception();
    } catch (JsonProcessingException ex) {
      log.error("[AUTH]: OAuth failed to parse OAuth2 client response", ex);
      throw Boom.serverError().message(ex.getMessage()).exception(ex);
    }
  }
}
