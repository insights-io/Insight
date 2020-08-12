package com.meemaw.auth.user.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class UserWithLoginInformation {

  UUID id;
  String email;
  String fullName;
  UserRole role;
  String organizationId;
  OffsetDateTime createdAt;
  String password;
  boolean tfaConfigured;

  public AuthUser user() {
    return new UserDTO(id, email, fullName, role, organizationId, createdAt);
  }
}
