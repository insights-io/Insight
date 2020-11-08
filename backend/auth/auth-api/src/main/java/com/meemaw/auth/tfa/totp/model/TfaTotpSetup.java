package com.meemaw.auth.tfa.totp.model;

import lombok.Value;

import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.model.TfaSetup;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
public class TfaTotpSetup implements TfaSetup {

  String secret;
  OffsetDateTime createdAt;
  UUID userId;

  @Override
  public TfaMethod getMethod() {
    return TfaMethod.TOTP;
  }
}
