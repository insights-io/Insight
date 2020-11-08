package com.meemaw.auth.organization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.RandomStringUtils;

import com.meemaw.auth.organization.model.dto.AvatarSetupDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.date.DateUtils;

import java.time.OffsetDateTime;

public interface Organization {

  int ID_LENGTH = 6;
  int BILLING_PERIOD_DURATION_DAYS = 30;

  String getId();

  String getName();

  boolean isOpenMembership();

  UserRole getDefaultRole();

  AvatarSetupDTO getAvatar();

  OffsetDateTime getCreatedAt();

  static String identifier() {
    return RandomStringUtils.randomAlphanumeric(ID_LENGTH);
  }

  @JsonIgnore
  default OffsetDateTime getStartOfCurrentBillingPeriod() {
    return DateUtils.getStartOfCurrentPeriod(getCreatedAt(), BILLING_PERIOD_DURATION_DAYS);
  }
}
