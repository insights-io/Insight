package com.meemaw.test.testconainers.api.auth;

import com.meemaw.test.testconainers.api.AbstractApiTestContainer;
import com.meemaw.test.testconainers.api.Api;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@Slf4j
public class AuthApiTestContainer extends AbstractApiTestContainer<AuthApiTestContainer> {

  private AuthApiTestContainer() {
    super(Api.AUTH);
  }

  /** @return auth api test container */
  public static AuthApiTestContainer newInstance() {
    return new AuthApiTestContainer()
        .withEnv("MAILER_MOCK", "true")
        .withLogConsumer(new Slf4jLogConsumer(log));
  }
}
