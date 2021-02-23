package com.rebrowse.auth.password.model;

import com.rebrowse.shared.model.CanExpire;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class PasswordResetRequest implements CanExpire {

  UUID token;
  UUID userId;
  String email;
  URL redirect;
  OffsetDateTime createdAt;

  @Override
  public int getDaysValidity() {
    return 1;
  }
}
