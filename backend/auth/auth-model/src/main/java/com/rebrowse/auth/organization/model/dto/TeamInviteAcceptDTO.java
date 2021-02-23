package com.rebrowse.auth.organization.model.dto;

import java.net.URL;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

  @NotNull(message = "Required")
  URL redirect;
}
