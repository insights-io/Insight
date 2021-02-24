package com.rebrowse.auth.user.model.dto;

import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.auth.organization.model.dto.OrganizationDTO;
import com.rebrowse.auth.user.model.AuthUser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserDataDTO {

  UserDTO user;
  OrganizationDTO organization;

  public static UserDataDTO from(AuthUser user, Organization organization) {
    return new UserDataDTO((UserDTO) user, (OrganizationDTO) organization);
  }
}
