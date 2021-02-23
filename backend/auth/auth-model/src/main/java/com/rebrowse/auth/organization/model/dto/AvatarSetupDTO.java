package com.rebrowse.auth.organization.model.dto;

import com.rebrowse.auth.organization.model.AvatarType;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AvatarSetupDTO {

  @NotNull(message = "Required")
  AvatarType type;

  String image;
}
