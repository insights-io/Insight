package com.meemaw.auth.tfa.sms.model;

import com.meemaw.auth.tfa.MfaMethod;
import com.meemaw.auth.tfa.model.MfaConfiguration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class SmsMfaSetup implements MfaConfiguration {

  OffsetDateTime createdAt;
  UUID userId;

  @Override
  public MfaMethod getMethod() {
    return MfaMethod.SMS;
  }
}
