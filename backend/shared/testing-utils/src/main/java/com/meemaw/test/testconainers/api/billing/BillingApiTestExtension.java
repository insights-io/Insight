package com.meemaw.test.testconainers.api.billing;

import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 compatible test resource.
 *
 * <p>USAGE: {@link BillingApi}
 */
public class BillingApiTestExtension implements BeforeAllCallback {

  private static final BillingApiTestContainer BILLING_API = BillingApiTestContainer.newInstance();

  public static BillingApiTestContainer getInstance() {
    return BILLING_API;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    start(BILLING_API).forEach(System::setProperty);
  }

  public static void stop() {
    BILLING_API.stop();
  }

  public static Map<String, String> start() {
    return start(BILLING_API);
  }

  public static Map<String, String> start(BillingApiTestContainer billingApi) {
    if (!billingApi.isRunning()) {
      System.out.println("[TEST-SETUP]: Starting billing-api container ...");
      billingApi.start();
    }

    String billingApiBaseURI = billingApi.getBaseURI();
    System.out.println(
        String.format("[TEST-SETUP]: Connecting to billing-api on=%s", billingApiBaseURI));

    return Map.of("billing-api/mp-rest/url", billingApiBaseURI);
  }
}
