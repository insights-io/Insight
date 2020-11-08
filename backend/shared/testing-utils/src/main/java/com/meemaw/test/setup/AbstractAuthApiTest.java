package com.meemaw.test.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.http.TestHTTPResource;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import javax.inject.Inject;

public abstract class AbstractAuthApiTest {

  @Inject protected MockMailbox mailbox;
  @Inject protected ObjectMapper objectMapper;
  @TestHTTPResource protected URI baseUri;

  public AuthApiTestProvider authApi() {
    String baseUrl = baseUri.toString();
    return new InternalAuthApiTestProvider(
        baseUrl.substring(0, baseUrl.length() - 1), objectMapper, mailbox);
  }

  @BeforeEach
  void init() {
    mailbox.clear();
  }
}
