package com.meemaw.auth.organization.invite.model.dto;

import com.meemaw.shared.validation.Password;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class InviteAcceptDTO {

  @NotBlank(message = "Required")
  String fullName;

  @Password String password;
}
