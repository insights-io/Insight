package com.rebrowse.test.testconainers.api.billing;

import com.rebrowse.test.testconainers.api.auth.AuthApiTestExtension;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

/**
 * Quarkus compatible test resource.
 *
 * <p>USAGE: @QuarkusTestResource(BillingApiTestResource.class)
 */
public class BillingApiTestResource implements QuarkusTestResourceLifecycleManager {

  @Override
  public Map<String, String> start() {
    return AuthApiTestExtension.start();
  }

  @Override
  public void stop() {
    AuthApiTestExtension.stop();
  }
}
