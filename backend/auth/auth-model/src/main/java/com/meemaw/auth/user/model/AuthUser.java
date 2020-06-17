package com.meemaw.auth.user.model;

import java.time.Instant;
import java.util.UUID;

public interface AuthUser {

  UUID getId();

  String getOrganizationId();

  UserRole getRole();

  String getEmail();

  String getFullName();

  Instant getCreatedAt();
}
