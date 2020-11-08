package com.rebrowse.net;

import com.rebrowse.model.ApiRequestParams;

import java.util.concurrent.CompletionStage;

public interface HttpClient {

  <T> CompletionStage<T> get(String url, Class<T> clazz, RequestOptions requestOptions);

  <T> CompletionStage<T> post(String url, Class<T> clazz, RequestOptions requestOptions);

  <P extends ApiRequestParams, T> CompletionStage<T> post(
      String url, P params, Class<T> clazz, RequestOptions requestOptions);

  <P extends ApiRequestParams, T> CompletionStage<T> patch(
      String url, P params, Class<T> clazz, RequestOptions requestOptions);
}
