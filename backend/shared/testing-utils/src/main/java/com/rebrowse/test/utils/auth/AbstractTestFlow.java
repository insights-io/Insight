package com.rebrowse.test.utils.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.net.RequestOptions;
import java.net.URI;

public abstract class AbstractTestFlow {

  protected final URI baseUri;
  protected final ObjectMapper objectMapper;

  public AbstractTestFlow(URI baseUri, ObjectMapper objectMapper) {
    this.baseUri = baseUri;
    this.objectMapper = objectMapper;
  }

  public RequestOptions.Builder sdkRequest() {
    return new RequestOptions.Builder().apiBaseUrl(baseUri.toString());
  }
}
