package com.meemaw.auth.sso.setup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SsoMethod {
  SAML("saml");

  private final String key;

  SsoMethod(String key) {
    this.key = key;
  }

  @JsonCreator
  public static SsoMethod fromString(String key) {
    return SsoMethod.valueOf(key == null ? null : key.toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return key;
  }
}
