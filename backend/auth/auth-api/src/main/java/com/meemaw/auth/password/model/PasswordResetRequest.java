package com.meemaw.auth.password.model;

import lombok.Value;

import com.meemaw.shared.model.CanExpire;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
public class PasswordResetRequest implements CanExpire {

  UUID token;
  UUID userId;
  String email;
  OffsetDateTime createdAt;

  @Override
  public int getDaysValidity() {
    return 1;
  }
}
