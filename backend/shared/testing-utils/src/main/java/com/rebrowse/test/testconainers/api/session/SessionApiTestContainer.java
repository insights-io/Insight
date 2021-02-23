package com.rebrowse.test.testconainers.api.session;

import com.rebrowse.test.testconainers.api.AbstractApiTestContainer;
import com.rebrowse.test.testconainers.api.Api;

public class SessionApiTestContainer extends AbstractApiTestContainer<SessionApiTestContainer> {

  private SessionApiTestContainer() {
    super(Api.SESSION);
  }

  public static SessionApiTestContainer newInstance() {
    return new SessionApiTestContainer();
  }
}
