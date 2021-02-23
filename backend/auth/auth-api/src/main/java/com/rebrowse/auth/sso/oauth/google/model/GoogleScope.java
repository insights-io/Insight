package com.rebrowse.auth.sso.oauth.google.model;

/** https://developers.google.com/identity/protocols/oauth2/scopes#oauth2 */
public enum GoogleScope {
  OPENID("openid"),
  EMAIL("email"),
  PROFILE("profile");

  private final String value;

  GoogleScope(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
