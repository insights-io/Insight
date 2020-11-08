package com.rebrowse.net;

import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.util.StringUtils;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
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
    this.headers = buildHeaders(this.requestOptions, bodyPublisher);
  }

  private static Map<String, List<String>> buildHeaders(
      RequestOptions options, BodyPublisher bodyPublisher) {
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

  public static RebrowseRequest get(String path, RequestOptions options) {
    return noBody(RequestMethod.GET, path, options);
  }

  public static RebrowseRequest post(String path, RequestOptions options) {
    return noBody(RequestMethod.POST, path, options);
  }

  public static <P extends ApiRequestParams> RebrowseRequest post(
      String path, P params, RequestOptions options) {
    return ofString(RequestMethod.POST, path, params, options);
  }

  public static RebrowseRequest patch(String path, RequestOptions options) {
    return noBody(RequestMethod.PATCH, path, options);
  }

  public static <P extends ApiRequestParams> RebrowseRequest patch(
      String path, P params, RequestOptions options) {
    return ofString(RequestMethod.PATCH, path, params, options);
  }

  private static RebrowseRequest noBody(
      RequestMethod method, String path, RequestOptions requestOptions) {
    return new RebrowseRequest(method, path, requestOptions, BodyPublishers.noBody());
  }

  private static <P extends ApiRequestParams> RebrowseRequest ofString(
      RequestMethod method, String path, P params, RequestOptions options) {
    BodyPublisher bodyPublisher =
        BodyPublishers.ofString(params.writeValueAsString(), StandardCharsets.UTF_8);
    return new RebrowseRequest(method, path, options, bodyPublisher);
  }
}
