package com.rebrowse.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.exception.ApiException;
import com.rebrowse.exception.RebrowseException;
import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.model.RebrowseOkDataResponse;
import com.rebrowse.model.error.RebrowseError;
import com.rebrowse.model.error.RebrowseErrorDataResponse;
import java.util.concurrent.CompletionStage;

public class RebrowseHttpClient implements HttpClient {

  private final RawHttpClient rawHttpClient;
  private final ObjectMapper objectMapper;

  public RebrowseHttpClient() {
    this(null, null);
  }

  public RebrowseHttpClient(RawHttpClient httpClient, ObjectMapper objectMapper) {
    this.rawHttpClient = (httpClient != null) ? httpClient : buildDefaultHttpClient();
    this.objectMapper = (objectMapper != null) ? objectMapper : ApiResource.OBJECT_MAPPER;
  }

  private RawHttpClient buildDefaultHttpClient() {
    return new NetHttpClient();
  }

  @Override
  public <T> CompletionStage<T> get(String url, Class<T> clazz, RequestOptions requestOptions) {
    return request(RebrowseRequest.get(url, requestOptions), clazz);
  }

  @Override
  public <P extends ApiRequestParams, T> CompletionStage<T> post(
      String url, P params, Class<T> clazz, RequestOptions requestOptions) {
    RebrowseRequest request = RebrowseRequest.post(url, params, requestOptions);
    return request(request, clazz);
  }

  @Override
  public <P extends ApiRequestParams, T> CompletionStage<T> patch(
      String url, P params, Class<T> clazz, RequestOptions requestOptions) {
    RebrowseRequest request = RebrowseRequest.patch(url, params, requestOptions);
    return request(request, clazz);
  }

  @Override
  public <T> CompletionStage<T> post(String url, Class<T> clazz, RequestOptions requestOptions) {
    RebrowseRequest request = RebrowseRequest.post(url, requestOptions);
    return request(request, clazz);
  }

  private <T> CompletionStage<T> request(RebrowseRequest request, Class<T> clazz) {
    return rawHttpClient
        .requestWithRetries(request)
        .thenApply(
            response -> {
              int statusCode = response.getStatusCode();
              String body = response.getBody();
              String requestId = response.requestId();

              if (statusCode < 200 || statusCode >= 300) {
                throw handleError(body, statusCode, requestId);
              }

              try {
                RebrowseOkDataResponse<T> okDataResponse =
                    objectMapper.readValue(
                        body,
                        objectMapper
                            .getTypeFactory()
                            .constructParametricType(RebrowseOkDataResponse.class, clazz));

                return okDataResponse.getData();
              } catch (JsonProcessingException ex) {
                throw malformedJsonError(body, statusCode, requestId, ex);
              }
            });
  }

  private RebrowseException handleError(String body, int statusCode, String requestId) {
    try {
      RebrowseError<?> apiError =
          ApiResource.OBJECT_MAPPER.readValue(body, RebrowseErrorDataResponse.class).getError();
      throw new ApiException(apiError, requestId, statusCode, null);
    } catch (JsonProcessingException ex) {
      return malformedJsonError(body, statusCode, requestId, ex);
    }
  }

  private RebrowseException malformedJsonError(
      String body, int statusCode, String requestId, JsonProcessingException exception) {
    String message =
        String.format(
            "Invalid response object from API: %s. (HTTP response code was %d)", body, statusCode);
    RebrowseError<?> apiError = new RebrowseError<>(statusCode, "Malformed JSON", message, null);
    return new ApiException(apiError, requestId, statusCode, exception);
  }
}
