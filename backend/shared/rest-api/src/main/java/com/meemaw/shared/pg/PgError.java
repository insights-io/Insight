package com.meemaw.shared.pg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum PgError {
  UNIQUE_VIOLATION("23505");

  private final String code;
}
