package com.rebrowse.auth.sso.setup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rebrowse.auth.sso.session.model.LoginMethod;

public enum SsoMethod {
  SAML(LoginMethod.SAML.getKey()),
  GOOGLE(LoginMethod.GOOGLE.getKey()),
  MICROSOFT(LoginMethod.MICROSOFT.getKey()),
  GITHUB(LoginMethod.GITHUB.getKey());

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
