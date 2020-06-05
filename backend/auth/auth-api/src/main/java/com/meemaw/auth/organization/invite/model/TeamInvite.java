package com.meemaw.auth.organization.invite.model;

import com.meemaw.auth.shared.CanExpire;
import com.meemaw.auth.user.model.UserRole;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class TeamInvite implements CanExpire {

  UUID token;
  String email;
  String organizationId;
  UserRole role;
  UUID creator;
  OffsetDateTime createdAt;
}
