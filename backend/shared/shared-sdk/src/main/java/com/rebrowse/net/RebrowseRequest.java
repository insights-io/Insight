package com.rebrowse.net;

import com.rebrowse.util.StringUtils;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Value;

@Value
public class RebrowseRequest {

  RequestMethod method;
  String path;
  Map<String, List<String>> headers;
  RequestOptions requestOptions;
  BodyPublisher bodyPublisher;

  private RebrowseRequest(
      RequestMethod method,
      String path,
      RequestOptions requestOptions,
      BodyPublisher bodyPublisher) {
    this.method = method;
    this.path = path;
    this.bodyPublisher = bodyPublisher;
    this.requestOptions =
        Optional.ofNullable(requestOptions).orElseGet(RequestOptions::createDefault);
    this.headers = buildHeaders(this.requestOptions);
  }

  private static Map<String, List<String>> buildHeaders(RequestOptions options) {
    Map<String, List<String>> headers = new HashMap<>();

    // Accept
    headers.put("Accept", Collections.singletonList("application/json"));

    // Accept-Charset
    headers.put("Accept-Charset", Collections.singletonList(ApiResource.CHARSET.name()));

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

  public static RebrowseRequest get(String path, RequestOptions requestOptions) {
    return new RebrowseRequest(RequestMethod.GET, path, requestOptions, BodyPublishers.noBody());
  }

  public static RebrowseRequest post(String path, String body, RequestOptions requestOptions) {
    return new RebrowseRequest(
        RequestMethod.POST, path, requestOptions, BodyPublishers.ofString(body));
  }

  public static RebrowseRequest post(String path, RequestOptions requestOptions) {
    return new RebrowseRequest(RequestMethod.POST, path, requestOptions, BodyPublishers.noBody());
  }
}
