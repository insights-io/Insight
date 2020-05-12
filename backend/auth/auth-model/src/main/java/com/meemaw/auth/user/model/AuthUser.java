package com.meemaw.auth.user.model;

import java.util.UUID;

public interface AuthUser {

  UUID getId();

  String getOrg();

  UserRole getRole();
}
