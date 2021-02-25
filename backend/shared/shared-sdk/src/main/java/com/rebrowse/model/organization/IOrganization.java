package com.rebrowse.model.organization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebrowse.api.date.DateUtils;
import com.rebrowse.model.user.UserRole;
import java.time.OffsetDateTime;

public interface IOrganization {

  int BILLING_PERIOD_DURATION_DAYS = 30;

  String getId();

  String getName();

  boolean isOpenMembership();

  boolean isEnforceMultiFactorAuthentication();

  UserRole getDefaultRole();

  AvatarType getAvatar();

  OffsetDateTime getCreatedAt();

  OffsetDateTime getUpdatedAt();

  @JsonIgnore
  default OffsetDateTime getStartOfCurrentBillingPeriod() {
    return DateUtils.getStartOfCurrentPeriod(getCreatedAt(), BILLING_PERIOD_DURATION_DAYS);
  }
}
