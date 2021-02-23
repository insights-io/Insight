package com.rebrowse.auth.sso.oauth.microsoft.model;

/**
 * https://docs.microsoft.com/en-gb/azure/active-directory/develop/v2-permissions-and-consent#openid-connect-scopes
 */
public enum MicrosoftScope {
  OPENID("openid"),
  EMAIL("email"),
  PROFILE("profile");

  private final String value;

  MicrosoftScope(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
