package com.meemaw.auth.sso.tfa.sms.model;

import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.setup.model.TfaSetup;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class SmsTfaSetup implements TfaSetup {

  OffsetDateTime createdAt;
  UUID userId;

  @Override
  public TfaMethod getMethod() {
    return TfaMethod.SMS;
  }
}
