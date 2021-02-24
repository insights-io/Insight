package com.rebrowse.auth.sso.session.model;

public enum LoginMethod {
  SAML("saml"),
  GOOGLE("google"),
  MICROSOFT("microsoft"),
  GITHUB("github"),
  PASSWORD("password");

  private final String key;

  LoginMethod(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
