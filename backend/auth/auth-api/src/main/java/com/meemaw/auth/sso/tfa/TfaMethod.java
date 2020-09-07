package com.meemaw.auth.sso.tfa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public enum TfaMethod {
  SMS("sms"),
  TOTP("totp");

  private final String key;

  TfaMethod(String key) {
    this.key = key;
  }

  @JsonCreator
  public static TfaMethod fromString(String key) {
    return TfaMethod.valueOf(Objects.requireNonNull(key).toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return key;
  }
}
