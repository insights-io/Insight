package com.meemaw.auth.user.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class UserWithHashedPassword {

  UUID id;
  String email;
  String fullName;
  UserRole role;
  String org;
  OffsetDateTime createdAt;
  String password;

  public AuthUser user() {
    return new UserDTO(id, email, fullName, role, org, createdAt);
  }
}
