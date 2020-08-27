package com.meemaw.test.testconainers.api.auth;

import com.meemaw.test.testconainers.api.AbstractApiTestContainer;
import com.meemaw.test.testconainers.api.Api;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthApiTestContainer extends AbstractApiTestContainer<AuthApiTestContainer> {

  private AuthApiTestContainer() {
    super(Api.AUTH);
  }

  public static AuthApiTestContainer newInstance() {
    return new AuthApiTestContainer().withEnv("MAILER_MOCK", "true");
  }
}
