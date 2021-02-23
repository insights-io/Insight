package com.rebrowse.auth.sso.setup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SamlMethod {
  CUSTOM("custom"),
  OKTA("okta"),
  ONELOGIN("onelogin"),
  AUTH0("auth0");

  private final String key;

  SamlMethod(String key) {
    this.key = key;
  }

  @JsonCreator
  public static SamlMethod fromString(String key) {
    return SamlMethod.valueOf(key == null ? null : key.toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return key;
  }
}
