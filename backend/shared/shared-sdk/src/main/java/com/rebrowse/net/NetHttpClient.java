package com.rebrowse.net;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class NetHttpClient extends RawHttpClient {

  private final HttpClient httpClient;

  public NetHttpClient() {
    this(HttpClient.newHttpClient());
  }

  public NetHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public CompletionStage<RebrowseResponse> request(RebrowseRequest request) {
    RequestOptions options = request.getRequestOptions();
    HttpRequest.Builder httpRequestBuilder =
        HttpRequest.newBuilder(request.getUri())
            .method(request.getMethod().toString(), request.getBodyPublisher())
            .timeout(options.getTimeout());

    for (Map.Entry<String, List<String>> entry : getHeaders(request).entrySet()) {
      String headerName = entry.getKey();
      for (String headerValue : entry.getValue()) {
        httpRequestBuilder = httpRequestBuilder.header(headerName, headerValue);
      }
    }

    return this.httpClient
        .sendAsync(httpRequestBuilder.build(), BodyHandlers.ofString())
        .thenApply(
            response ->
                new RebrowseResponse(response.statusCode(), response.headers(), response.body()));
  }

  private static Map<String, List<String>> getHeaders(RebrowseRequest request) {
    Map<String, List<String>> headers = request.getHeaders();

    // User Agent
    headers.put("User-Agent", Collections.singletonList(RawHttpClient.userAgentString()));
    headers.put(
        "X-Rebrowse-Client-User-Agent",
        Collections.singletonList(RawHttpClient.clientUserAgentString()));

    return headers;
  }
}
