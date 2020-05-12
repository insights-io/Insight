package com.meemaw.auth.user.model;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserDTO implements AuthUser {

  UUID id;
  String email;
  UserRole role;
  String org;
}
