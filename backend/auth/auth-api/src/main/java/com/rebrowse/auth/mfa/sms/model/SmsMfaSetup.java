package com.rebrowse.auth.mfa.sms.model;

import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.model.MfaConfiguration;
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
