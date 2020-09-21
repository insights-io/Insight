package com.meemaw.test.testconainers.api.billing;

import com.meemaw.test.testconainers.api.AbstractApiTestContainer;
import com.meemaw.test.testconainers.api.Api;

public class BillingApiTestContainer extends AbstractApiTestContainer<BillingApiTestContainer> {

  private BillingApiTestContainer() {
    super(Api.BILLING);
  }

  public static BillingApiTestContainer newInstance() {
    return new BillingApiTestContainer();
  }
}
