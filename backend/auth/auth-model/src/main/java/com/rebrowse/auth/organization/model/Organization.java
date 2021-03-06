package com.rebrowse.auth.organization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebrowse.api.date.DateUtils;
import com.rebrowse.auth.organization.model.dto.AvatarSetupDTO;
import com.rebrowse.auth.user.model.UserRole;
import java.time.OffsetDateTime;
import org.apache.commons.lang3.RandomStringUtils;

public interface Organization {

  int ID_LENGTH = 6;
  int BILLING_PERIOD_DURATION_DAYS = 30;

  static String identifier() {
    return RandomStringUtils.randomAlphanumeric(ID_LENGTH);
  }

  String getId();

  String getName();

  boolean isOpenMembership();

  boolean isEnforceMultiFactorAuthentication();

  UserRole getDefaultRole();

  AvatarSetupDTO getAvatar();

  OffsetDateTime getCreatedAt();

  @JsonIgnore
  default OffsetDateTime getStartOfCurrentBillingPeriod() {
    return DateUtils.getStartOfCurrentPeriod(getCreatedAt(), BILLING_PERIOD_DURATION_DAYS);
  }
}
