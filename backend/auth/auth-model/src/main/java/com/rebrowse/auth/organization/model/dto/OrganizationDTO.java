package com.rebrowse.auth.organization.model.dto;

import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.auth.user.model.UserRole;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OrganizationDTO implements Organization {

  String id;
  String name;
  boolean openMembership;
  boolean enforceMultiFactorAuthentication;
  UserRole defaultRole;
  AvatarSetupDTO avatar;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;
}
