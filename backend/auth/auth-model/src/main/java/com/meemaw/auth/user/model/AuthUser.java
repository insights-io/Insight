package com.meemaw.auth.user.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AuthUser {

  UUID getId();

  String getOrg();

  UserRole getRole();

  String getEmail();

  String getFullName();

  OffsetDateTime getCreatedAt();
}
