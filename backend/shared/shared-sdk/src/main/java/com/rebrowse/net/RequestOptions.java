package com.rebrowse.net;

import com.rebrowse.Rebrowse;
import java.time.Duration;
import lombok.Value;

@Value
public class RequestOptions {

  String apiKey;
  String sessionId;
  Duration timeout;
  int maxNetworkRetries;

  public static RequestOptions createDefault() {
    return new RequestOptions.Builder().build();
  }

  public static final class Builder {

    private String sessionId = null;
    private String apiKey;
    private final Duration timeout;
    private final int maxNetworkRetries;

    public Builder() {
      this.apiKey = Rebrowse.apiKey;
      this.timeout = Rebrowse.TIMEOUT;
      this.maxNetworkRetries = Rebrowse.maxNetworkRetries();
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
      return new RequestOptions(apiKey, sessionId, timeout, maxNetworkRetries);
    }
  }
}
