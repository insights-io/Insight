package com.rebrowse.net;

import lombok.Value;

import java.net.http.HttpHeaders;
import java.util.Objects;

@Value
public class RebrowseResponse {

  int statusCode;
  HttpHeaders headers;
  String body;

  public RebrowseResponse(int statusCode, HttpHeaders headers, String body) {
    Objects.requireNonNull(headers);
    Objects.requireNonNull(body);

    this.statusCode = statusCode;
    this.headers = headers;
    this.body = body;
  }

  /**
   * Gets the ID of the request, as returned by Rebrowse.
   *
   * @return the ID of the request, as returned by Rebrowse
   */
  public String requestId() {
    return this.headers.firstValue("X-Request-Id").orElse(null);
  }
}
