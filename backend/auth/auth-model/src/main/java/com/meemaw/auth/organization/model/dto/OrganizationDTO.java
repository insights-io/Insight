package com.meemaw.auth.organization.model.dto;

import com.meemaw.auth.billing.model.SubscriptionPlan;
import com.meemaw.auth.organization.model.Organization;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class OrganizationDTO implements Organization {

  String id;
  String name;
  SubscriptionPlan plan;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;
}
