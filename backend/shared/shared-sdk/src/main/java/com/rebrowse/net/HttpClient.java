package com.rebrowse.net;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rebrowse.model.ApiRequestParams;
import java.util.concurrent.CompletionStage;

public interface HttpClient {

  <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String url, P params, Class<R> clazz, RequestOptions options);

  <R, P extends ApiRequestParams> CompletionStage<R> request(
      RequestMethod method, String url, P params, TypeReference<R> clazz, RequestOptions options);
}
