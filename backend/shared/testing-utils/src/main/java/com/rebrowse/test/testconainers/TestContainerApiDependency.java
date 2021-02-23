package com.rebrowse.test.testconainers;

import com.rebrowse.test.testconainers.api.AbstractApiTestContainer;

public interface TestContainerApiDependency {

  void inject(AbstractApiTestContainer<?> apiContainer);
}
