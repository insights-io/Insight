package com.rebrowse.net;

import lombok.Value;

import com.rebrowse.Rebrowse;

import java.time.Duration;

@Value
public class RequestOptions {

  String apiKey;
  String sessionId;
  String apiBaseUrl;
  Duration timeout;
  int maxNetworkRetries;

  public static RequestOptions createDefault() {
    return new RequestOptions.Builder().build();
  }

  public static final class Builder {

    private String sessionId = null;
    private String apiKey;
    private String apiBaseUrl;
    private final Duration timeout;
    private final int maxNetworkRetries;

    public Builder() {
      this.apiKey = Rebrowse.apiKey;
      this.timeout = Rebrowse.TIMEOUT;
      this.apiBaseUrl = Rebrowse.API_BASE;
      this.maxNetworkRetries = Rebrowse.maxNetworkRetries();
    }

    public Builder apiBaseUrl(String apiBaseUrl) {
      this.apiBaseUrl = apiBaseUrl;
      return this;
    }

    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public RequestOptions build() {
      return new RequestOptions(apiKey, sessionId, apiBaseUrl, timeout, maxNetworkRetries);
    }
  }
}
