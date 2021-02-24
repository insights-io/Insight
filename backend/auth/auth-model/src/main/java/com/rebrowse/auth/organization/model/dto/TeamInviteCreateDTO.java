package com.rebrowse.auth.organization.model.dto;

import com.rebrowse.auth.user.model.UserRole;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TeamInviteCreateDTO {

  @NotBlank(message = "Required")
  @Email
  String email;

  @NotNull(message = "Required")
  UserRole role;
}
