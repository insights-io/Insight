package com.rebrowse.net;

import com.rebrowse.Rebrowse;
import java.time.Duration;
import lombok.Value;

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

  public static RequestOptions.Builder sessionId(String sessionId) {
    return new Builder().sessionId(sessionId);
  }

  public static final class Builder {

    private final Duration timeout;
    private final int maxNetworkRetries;
    private String sessionId = null;
    private String apiKey;
    private String apiBaseUrl;

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
