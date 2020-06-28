package com.meemaw.test.testconainers.api.session;

import com.meemaw.test.testconainers.api.AbstractApiTestContainer;
import com.meemaw.test.testconainers.api.Api;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionApiTestContainer extends AbstractApiTestContainer<SessionApiTestContainer> {

  private SessionApiTestContainer() {
    super(Api.SESSION);
  }

  /** @return */
  public static SessionApiTestContainer newInstance() {
    return new SessionApiTestContainer();
  }
}
