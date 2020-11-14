package com.rebrowse.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
  public <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String path, P params, Class<R> clazz, RequestOptions requestOptions) {
    return request(
        new RebrowseRequest(method, path, requestOptions, params),
        (body) -> dataResponseType(body, clazz));
  }

  @Override
  public <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method,
      String path,
      P params,
      TypeReference<R> clazz,
      RequestOptions requestOptions) {
    return request(
        new RebrowseRequest(method, path, requestOptions, params),
        (body) -> dataResponseType(body, clazz));
  }

  private <T> CompletionStage<T> request(
      RebrowseRequest request, ReadValue<String, RebrowseOkDataResponse<T>> readValue) {
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

              if (statusCode == 204) {
                return null;
              }

              try {
                RebrowseOkDataResponse<T> dataResponse = readValue.apply(body);
                return dataResponse.getData();
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

  private <R> RebrowseOkDataResponse<R> dataResponseType(String body, Class<R> clazz)
      throws JsonProcessingException {
    return objectMapper.readValue(
        body,
        objectMapper.getTypeFactory().constructParametricType(RebrowseOkDataResponse.class, clazz));
  }

  private <R> RebrowseOkDataResponse<R> dataResponseType(
      String body, TypeReference<R> typeReference) throws JsonProcessingException {
    return objectMapper.readValue(
        body,
        objectMapper
            .getTypeFactory()
            .constructParametricType(
                RebrowseOkDataResponse.class, objectMapper.constructType(typeReference.getType())));
  }
}
