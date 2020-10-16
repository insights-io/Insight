package com.rebrowse.model.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public enum UserRole {
  ADMIN("admin"),
  MEMBER("member");

  private final String key;

  UserRole(String key) {
    this.key = key;
  }

  @JsonCreator
  public static UserRole fromString(String key) {
    return UserRole.valueOf(Objects.requireNonNull(key).toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return key;
  }
}
