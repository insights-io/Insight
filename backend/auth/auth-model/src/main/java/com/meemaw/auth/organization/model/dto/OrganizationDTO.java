package com.meemaw.auth.organization.model.dto;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.user.model.UserRole;
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
  UserRole defaultRole;
  AvatarSetupDTO avatar;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;
}
