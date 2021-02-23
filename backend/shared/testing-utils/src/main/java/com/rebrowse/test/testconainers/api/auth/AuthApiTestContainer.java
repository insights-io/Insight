package com.rebrowse.test.testconainers.api.auth;

import com.rebrowse.test.testconainers.TestContainerApiDependency;
import com.rebrowse.test.testconainers.api.AbstractApiTestContainer;
import com.rebrowse.test.testconainers.api.Api;

public class AuthApiTestContainer extends AbstractApiTestContainer<AuthApiTestContainer>
    implements TestContainerApiDependency {

  private AuthApiTestContainer() {
    super(Api.AUTH);
  }

  public static AuthApiTestContainer newInstance() {
    return new AuthApiTestContainer().withEnv("MAILER_MOCK", "true");
  }

  @Override
  public void inject(AbstractApiTestContainer<?> container) {
    container.withEnv("auth-api/mp-rest/url", getDockerBaseUri().toString());
  }
}
