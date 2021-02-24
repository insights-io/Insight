package com.rebrowse.auth.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.exception.ApiException;
import com.rebrowse.model.auth.SsoMethod;
import com.rebrowse.model.auth.SsoSetup;
import com.rebrowse.model.auth.SsoSetupCreateParams;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import java.net.URI;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Assertions;

public final class SsoSetupFlows extends AbstractTestFlow {

  public SsoSetupFlows(URI baseUri, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
  }

  public SsoSetup create(SsoMethod method, String sessionId) {
    return create(SsoSetupCreateParams.builder().method(method).build(), sessionId);
  }

  public SsoSetup create(SsoSetupCreateParams params, String sessionId) {
    return SsoSetup.create(params, sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();
  }

  public ApiException createAlreadyExists(SsoSetupCreateParams params, String sessionId)
      throws JsonProcessingException {
    CompletionException exception =
        Assertions.assertThrows(CompletionException.class, () -> create(params, sessionId));

    ApiException apiException = (ApiException) exception.getCause();
    Assertions.assertEquals(
        objectMapper.writeValueAsString(apiException.getApiError()),
        "{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"SSO setup already configured\"}");

    return apiException;
  }
}
