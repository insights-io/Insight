package com.meemaw.auth.tfa.sms.model;

import lombok.Value;

import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.model.TfaSetup;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
public class SmsTfaSetup implements TfaSetup {

  OffsetDateTime createdAt;
  UUID userId;

  @Override
  public TfaMethod getMethod() {
    return TfaMethod.SMS;
  }
}
