package com.meemaw.auth.user.model.dto;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.user.model.AuthUser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SessionInfoDTO {

  UserDTO user;
  OrganizationDTO organization;

  public static SessionInfoDTO from(AuthUser user, Organization organization) {
    return new SessionInfoDTO((UserDTO) user, (OrganizationDTO) organization);
  }
}
