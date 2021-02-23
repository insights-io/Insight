package com.rebrowse.shared.sql.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SqlErrorCode {
  UNIQUE_VIOLATION("23505");

  @Getter private final String code;
}
