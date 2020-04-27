package com.meemaw.shared.pg;

public enum PgError {

  UNIQUE_VIOLATION("23505");

  private String code;

  PgError(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
