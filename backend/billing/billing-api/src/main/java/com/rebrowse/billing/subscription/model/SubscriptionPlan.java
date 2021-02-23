package com.rebrowse.billing.subscription.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public enum SubscriptionPlan {
  FREE("free"),
  BUSINESS("business"),
  ENTERPRISE("enterprise");

  private final String key;

  SubscriptionPlan(String key) {
    this.key = key;
  }

  @JsonCreator
  public static SubscriptionPlan fromString(String key) {
    return SubscriptionPlan.valueOf(Objects.requireNonNull(key).toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return key;
  }
}
