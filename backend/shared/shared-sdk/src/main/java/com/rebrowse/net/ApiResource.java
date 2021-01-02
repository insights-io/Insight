package com.rebrowse.net;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.api.JacksonUtils;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.model.ApiRequestParams;
import java.nio.charset.Charset;
import java.util.concurrent.CompletionStage;

public final class ApiResource {

  public static final Charset CHARSET = RebrowseApi.CHARSET;
  public static final ObjectMapper OBJECT_MAPPER = JacksonUtils.configureClient(new ObjectMapper());

  private static HttpClient httpClient = new RebrowseHttpClient();

  private ApiResource() {}

  public static void setHttpClient(HttpClient httpClient) {
    ApiResource.httpClient = httpClient;
  }

  public static <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String url, P params, Class<R> clazz, RequestOptions options) {
    return httpClient.request(method, url, params, clazz, options);
  }

  public static <R> CompletionStage<R> request(
      RequestMethod method, String url, Class<R> clazz, RequestOptions options) {
    return request(method, url, null, clazz, options);
  }

  public static <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String url, P params, TypeReference<R> clazz, RequestOptions options) {
    return httpClient.request(method, url, params, clazz, options);
  }

  public static <R> CompletionStage<R> request(
      RequestMethod method, String url, TypeReference<R> clazz, RequestOptions options) {
    return request(method, url, null, clazz, options);
  }
}
