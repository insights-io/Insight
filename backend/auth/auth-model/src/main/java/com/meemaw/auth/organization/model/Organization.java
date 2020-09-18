package com.meemaw.auth.organization.model;

import com.meemaw.auth.billing.model.SubscriptionPlan;
import java.time.OffsetDateTime;
import org.apache.commons.lang3.RandomStringUtils;

public interface Organization {

  int ID_LENGTH = 6;

  String getId();

  String getName();

  SubscriptionPlan getPlan();

  OffsetDateTime getCreatedAt();

  static String identifier() {
    return RandomStringUtils.randomAlphanumeric(ID_LENGTH);
  }
}
