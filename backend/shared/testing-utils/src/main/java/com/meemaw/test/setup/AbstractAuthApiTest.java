package com.meemaw.test.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.mailer.MockMailbox;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractAuthApiTest {

  @Inject protected MockMailbox mailbox;
  @Inject protected ObjectMapper objectMapper;

  public AuthApiTestProvider authApi() {
    return new InternalAuthApiTestProvider(objectMapper, mailbox);
  }

  @BeforeEach
  void init() {
    mailbox.clear();
  }
}
