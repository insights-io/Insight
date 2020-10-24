package com.meemaw.auth.organization.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.model.CanExpire;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamInviteDTO implements CanExpire {

  UUID token;
  String email;
  String organizationId;
  UserRole role;
  UUID creator;
  OffsetDateTime createdAt;
}
