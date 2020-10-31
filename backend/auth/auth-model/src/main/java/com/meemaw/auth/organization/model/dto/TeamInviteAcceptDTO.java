package com.meemaw.auth.organization.model.dto;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TeamInviteAcceptDTO {

  @NotBlank(message = "Required")
  String fullName;

  @NotBlank(message = "Required")
  String password;
}
