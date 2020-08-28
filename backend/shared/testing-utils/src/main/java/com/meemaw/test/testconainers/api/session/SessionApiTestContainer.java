package com.meemaw.test.testconainers.api.session;

import com.meemaw.test.testconainers.api.AbstractApiTestContainer;
import com.meemaw.test.testconainers.api.Api;

public class SessionApiTestContainer extends AbstractApiTestContainer<SessionApiTestContainer> {

  private SessionApiTestContainer() {
    super(Api.SESSION);
  }

  public static SessionApiTestContainer newInstance() {
    return new SessionApiTestContainer();
  }
}
