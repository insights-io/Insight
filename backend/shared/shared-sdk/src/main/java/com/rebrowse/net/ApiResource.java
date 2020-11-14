package com.rebrowse.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rebrowse.model.ApiRequestParams;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

public final class ApiResource {

  public static final Charset CHARSET = StandardCharsets.UTF_8;
  public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

  private static HttpClient httpClient = new RebrowseHttpClient();

  private ApiResource() {}

  public static void setHttpClient(HttpClient httpClient) {
    ApiResource.httpClient = httpClient;
  }

  private static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return mapper;
  }

  public static <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String url, P params, Class<R> clazz, RequestOptions options) {
    return httpClient.request(method, url, params, clazz, options);
  }

  public static <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String url, Class<R> clazz, RequestOptions options) {
    return request(method, url, null, clazz, options);
  }

  public static <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String url, P params, TypeReference<R> clazz, RequestOptions options) {
    return httpClient.request(method, url, params, clazz, options);
  }

  public static <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String url, TypeReference<R> clazz, RequestOptions options) {
    return request(method, url, null, clazz, options);
  }
}
