package com.rebrowse.model.organization;

import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.model.user.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrganizationUpdateParams implements ApiRequestParams {

  String name;
  UserRole defaultRole;
  boolean openMembership;
  boolean enforceTwoFactorAuthentication;
}
