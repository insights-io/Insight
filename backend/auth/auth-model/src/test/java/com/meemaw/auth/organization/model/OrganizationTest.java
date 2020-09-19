package com.meemaw.auth.organization.model;

import com.meemaw.auth.billing.model.SubscriptionPlan;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrganizationTest {

  private Organization organizationWithStartTime(OffsetDateTime createdAt) {
    return new OrganizationDTO("", "", SubscriptionPlan.FREE, createdAt, OffsetDateTime.now());
  }

  @Test
  public void organization__should_correctly_calculate_start_of_billing_period() {
    OffsetDateTime now = OffsetDateTime.now();
    Assertions.assertEquals(now, organizationWithStartTime(now).getStartOfCurrentBillingPeriod());
  }
}
