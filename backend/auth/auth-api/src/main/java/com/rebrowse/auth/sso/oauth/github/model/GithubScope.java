package com.rebrowse.auth.sso.oauth.github.model;

/**
 * https://developer.github.com/apps/building-oauth-apps/understanding-scopes-for-oauth-apps/#available-scopes
 */
public enum GithubScope {
  READ_USER("read:user"),
  USER_EMAIL("user:email");

  private final String value;

  GithubScope(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
