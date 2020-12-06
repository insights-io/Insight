package com.meemaw.test.testconainers;

import com.meemaw.test.testconainers.api.Api;
import org.testcontainers.containers.GenericContainer;

public interface TestContainerApiDependency {

  void inject(Api api, GenericContainer<?> apiContainer);
}
