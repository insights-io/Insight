package com.meemaw.test.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;

public abstract class ExternalAuthApiProvidedTest {

  protected @Inject ObjectMapper objectMapper;

  public AuthApiTestProvider authApi() {
    return new ExternalAuthApiSsoTestProvider(objectMapper);
  }
}
