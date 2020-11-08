package com.meemaw.auth.organization.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.auth.organization.model.AvatarType;

import javax.validation.constraints.NotNull;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AvatarSetupDTO {

  @NotNull(message = "Required")
  AvatarType type;

  String image;
}
