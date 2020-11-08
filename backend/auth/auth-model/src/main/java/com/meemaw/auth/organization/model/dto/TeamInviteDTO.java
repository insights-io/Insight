package com.meemaw.auth.organization.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.model.CanExpire;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamInviteDTO implements CanExpire {

  public static final int DAYS_VALIDITY = 1;

  UUID token;
  String email;
  String organizationId;
  UserRole role;
  UUID creator;
  OffsetDateTime createdAt;

  @Override
  @JsonIgnore
  public int getDaysValidity() {
    return DAYS_VALIDITY;
  }
}
