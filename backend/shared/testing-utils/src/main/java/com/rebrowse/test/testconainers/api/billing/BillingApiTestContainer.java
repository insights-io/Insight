package com.rebrowse.test.testconainers.api.billing;

import com.rebrowse.test.testconainers.api.AbstractApiTestContainer;
import com.rebrowse.test.testconainers.api.Api;

public class BillingApiTestContainer extends AbstractApiTestContainer<BillingApiTestContainer> {

  private BillingApiTestContainer() {
    super(Api.BILLING);
  }

  public static BillingApiTestContainer newInstance() {
    return new BillingApiTestContainer();
  }
}
