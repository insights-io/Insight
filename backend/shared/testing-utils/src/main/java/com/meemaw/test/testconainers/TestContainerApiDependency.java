package com.meemaw.test.testconainers;

import com.meemaw.test.testconainers.api.AbstractApiTestContainer;

public interface TestContainerApiDependency {

  void inject(AbstractApiTestContainer<?> apiContainer);
}
