package com.meemaw.auth.sso.tfa.totp.model;

import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.setup.model.TfaSetup;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

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
