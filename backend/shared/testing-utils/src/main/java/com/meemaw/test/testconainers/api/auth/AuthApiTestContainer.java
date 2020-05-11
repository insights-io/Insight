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

  /** @return */
  public static AuthApiTestContainer newInstance() {
    return new AuthApiTestContainer().withLogConsumer(new Slf4jLogConsumer(log));
  }
}
