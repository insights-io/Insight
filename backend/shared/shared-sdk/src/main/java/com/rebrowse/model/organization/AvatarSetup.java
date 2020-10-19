package com.rebrowse.model.organization;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class AvatarSetup {

  AvatarType type;
  String image;
}
