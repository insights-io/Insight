package com.meemaw.shared.auth;

import java.util.UUID;

public interface AuthUser {

  UUID getId();

  String getOrg();

  UserRole getRole();
}
