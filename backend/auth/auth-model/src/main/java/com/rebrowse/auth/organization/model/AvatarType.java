package com.rebrowse.auth.organization.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public enum AvatarType {
  INITIALS("initials"),
  AVATAR("avatar");

  private final String key;

  AvatarType(String key) {
    this.key = key;
  }

  @JsonCreator
  public static AvatarType fromString(String key) {
    return AvatarType.valueOf(Objects.requireNonNull(key).toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return key;
  }
}
