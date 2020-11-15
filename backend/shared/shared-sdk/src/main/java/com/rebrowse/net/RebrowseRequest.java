package com.rebrowse.net;

import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.util.FormEncoder;
import com.rebrowse.util.StringUtils;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Value;

@Value
public class RebrowseRequest {

  RequestMethod method;
  URI uri;
  Map<String, List<String>> headers;
  RequestOptions options;
  HttpRequest.BodyPublisher bodyPublisher;

  public RebrowseRequest(
      RequestMethod method, String path, RequestOptions maybeOptions, ApiRequestParams params) {
    this.method = method;
    this.bodyPublisher = bodyPublisher(params);
    this.options = Optional.ofNullable(maybeOptions).orElseGet(RequestOptions::createDefault);
    this.headers = buildHeaders(this.options, bodyPublisher);
    this.uri = buildURI(options.getApiBaseUrl(), path, method, params);
  }

  private static Map<String, List<String>> buildHeaders(
      RequestOptions options, HttpRequest.BodyPublisher bodyPublisher) {
    Map<String, List<String>> headers = new HashMap<>();

    // Accept
    headers.put("Accept", Collections.singletonList("application/json"));

    // Accept-Charset
    headers.put("Accept-Charset", Collections.singletonList(ApiResource.CHARSET.name()));

    // Content-Type
    if (bodyPublisher.contentLength() > 0) {
      headers.put("Content-Type", Collections.singletonList("application/json"));
    }

    String sessionId = options.getSessionId();
    String apiKey = options.getApiKey();

    if (isTokenUsable(sessionId)) {
      headers.put("Cookie", Collections.singletonList(String.format("SessionId=%s", sessionId)));
    } else if (isTokenUsable(apiKey)) {
      headers.put("Authorization", Collections.singletonList(String.format("Bearer %s", apiKey)));
    }

    return headers;
  }

  private static boolean isTokenUsable(String token) {
    return token != null && !token.isEmpty() && !StringUtils.containsWhitespace(token);
  }

  private <P extends ApiRequestParams> URI buildURI(
      String apiBaseUrl, String path, RequestMethod method, P params) {
    StringBuilder sb = new StringBuilder(apiBaseUrl).append(path);
    if (params != null && method.equals(RequestMethod.GET)) {
      String queryString = FormEncoder.createQueryString(params.toMap());
      if (!queryString.isBlank()) {
        sb.append("?");
        sb.append(queryString);
      }
    }

    return URI.create(sb.toString());
  }

  private <P extends ApiRequestParams> HttpRequest.BodyPublisher bodyPublisher(P params) {
    return params == null
        ? HttpRequest.BodyPublishers.noBody()
        : HttpRequest.BodyPublishers.ofString(params.writeValueAsString(), ApiResource.CHARSET);
  }
}
