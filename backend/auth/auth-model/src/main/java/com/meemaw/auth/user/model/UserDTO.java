package com.meemaw.auth.user.model;

import java.time.OffsetDateTime;
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
  String fullName;
  UserRole role;
  String org;
  OffsetDateTime createdAt;
}
