package com.meemaw.auth.password.model;

import com.meemaw.auth.shared.CanExpire;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class PasswordResetRequest implements CanExpire {

  UUID token;
  UUID userId;
  String email;
  OffsetDateTime createdAt;
}
