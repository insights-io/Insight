package com.rebrowse.net;

import java.util.concurrent.CompletionStage;

public interface HttpClient {

  <T> CompletionStage<T> get(String url, Class<T> clazz, RequestOptions requestOptions);

  <T> CompletionStage<T> post(String url, Class<T> clazz, RequestOptions requestOptions);
}
