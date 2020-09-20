package com.meemaw.auth.organization.model;

import com.meemaw.auth.billing.model.SubscriptionPlan;
import lombok.Value;

@Value
public class CreateOrganizationParams {

  String id;
  String name;
  SubscriptionPlan plan;

  public static CreateOrganizationParams freePlan(String id, String name) {
    return new CreateOrganizationParams(id, name, SubscriptionPlan.FREE);
  }
}
