package com.rebrowse.auth.mfa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public enum MfaMethod {
  SMS("sms"),
  TOTP("totp");

  public static int NUM_METHODS = MfaMethod.values().length;

  private final String key;

  MfaMethod(String key) {
    this.key = key;
  }

  @JsonCreator
  public static MfaMethod fromString(String key) {
    return MfaMethod.valueOf(Objects.requireNonNull(key).toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return key;
  }
}
