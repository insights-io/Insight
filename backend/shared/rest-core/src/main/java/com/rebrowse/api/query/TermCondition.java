package com.rebrowse.api.query;

import lombok.ToString;

@ToString
public enum TermCondition {
  EQ,
  GT,
  GTE,
  LT,
  LTE;

  public String getKey() {
    return name().toLowerCase();
  }
}
