package com.rebrowse.net;

import static com.rebrowse.api.RebrowseApi.REQUEST_ID_HEADER;

import java.net.http.HttpHeaders;
import java.util.Objects;
import lombok.Value;

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
    return this.headers.firstValue(REQUEST_ID_HEADER).orElse(null);
  }
}
