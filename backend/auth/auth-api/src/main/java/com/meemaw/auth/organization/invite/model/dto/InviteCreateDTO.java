package com.meemaw.auth.organization.invite.model.dto;

import com.meemaw.auth.user.model.UserRole;
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
public class InviteCreateDTO {

  @NotBlank(message = "Required")
  @Email
  String email;

  @NotNull(message = "Required")
  UserRole role;
}
