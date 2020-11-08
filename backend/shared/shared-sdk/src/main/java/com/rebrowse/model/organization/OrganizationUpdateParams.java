package com.rebrowse.model.organization;

import lombok.Builder;
import lombok.Value;

import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.model.user.UserRole;

@Value
@Builder
public class OrganizationUpdateParams implements ApiRequestParams {

  String name;
  UserRole defaultRole;
  boolean openMembership;
}
